package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;

import java.util.*;

/**
 * Created by adandris on 10.05.17.
 */
public class SiteMapper extends AbstractVerticle {
    /**
     * Wait up to 5 minutes for a message reply.
     */
    private static final int SEND_TIMEOUT = 5 * 60 * 1000;

    private Map<String, PageInfo> pages = new HashMap<>();

    private Map<String, Integer> pageHits = new HashMap<>();

    private Queue<String> urlsToCrawl = new LinkedList<>();

    private Set<String> pendingUrls = new HashSet<>();

    private int totalHits;

    private int pagesCrawled;

    @Override
    public void start() {
        vertx.eventBus().consumer(Events.MAP_SITE, message -> {
            urlsToCrawl.add(message.body().toString());

            crawl();

            vertx.setPeriodic(1000, event -> {
                System.out.println("Pages crawled: " + pagesCrawled);

                if (urlsToCrawl.peek() == null) {
                    if (pendingUrls.size() == 0) {
                        pages.values().forEach(page -> page.setPriority((double) pageHits.get(page.getUrl()) / totalHits));
                        message.reply(new JsonArray(new ArrayList<>(pages.values())));
                    }
                } else {
                    crawl();
                }
            });
        });
    }

    private void crawl() {
        while (urlsToCrawl.peek() != null) {
            String url = urlsToCrawl.remove();

            if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".jpeg")) {
                continue;
            }

            pendingUrls.add(url);

            vertx.eventBus().<PageInfo>send(Events.CRAWL_PAGE, url, new DeliveryOptions().setSendTimeout(SEND_TIMEOUT),
                    event -> {
                        pendingUrls.remove(url);

                        if (event.succeeded()) {
                            pagesCrawled++;

                            Message<PageInfo> message = event.result();
                            PageInfo pageInfo = new PageInfo(message.body());

                            incrementPageHits(url);

                            pages.put(url, pageInfo);

                            crawlChildren(pageInfo);
                        } else {
                            System.out.println(url + ": " + event.cause().getMessage());
                        }
                    });
        }
    }

    private void crawlChildren(PageInfo pageInfo) {
        pageInfo.getChildren().forEach(childUrl -> {
            incrementPageHits(childUrl);

            if (!pages.containsKey(childUrl) && !urlsToCrawl.contains(childUrl) && !pendingUrls.contains(childUrl)) {
                urlsToCrawl.add(childUrl);
            }
        });
    }

    private void incrementPageHits(String childUrl) {
        if (pageHits.containsKey(childUrl)) {
            pageHits.put(childUrl, pageHits.get(childUrl) + 1);
        } else {
            pageHits.put(childUrl, 1);
        }
        totalHits++;
    }
}
