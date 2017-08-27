package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by adandris on 10.05.17.
 */
public class SiteMapper extends AbstractVerticle {
    private static final Logger logger = LogManager.getLogger(SiteMapper.class.getName());

    /**
     * Wait up to 20 minutes for a message reply.
     */
    private static final long SEND_TIMEOUT = TimeUnit.MINUTES.toMillis(20);

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
                        calculatePriority();

                        message.reply(new JsonArray(new ArrayList<>(pages.values())));
                    }
                } else {
                    crawl();
                }
            });
        });
    }

    private void calculatePriority() {
        final int maxElement = Collections.max(pageHits.values());
        double orderOfMagnitude = 1.0;
        while (maxElement * orderOfMagnitude / totalHits < 0.1) {
            orderOfMagnitude *= 10.0;
        }
        final double factor = orderOfMagnitude;
        pages.values().forEach(page -> page.setPriority((double) pageHits.get(page.getUrl()) * factor / totalHits));
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
                            logger.error(url + ": " + event.cause().getMessage());
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
