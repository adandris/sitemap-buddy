package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;

import java.util.*;

/**
 * Created by adandris on 10.05.17.
 */
public class SiteMapper extends AbstractVerticle {
    private Map<String, PageInfo> pages = new HashMap<>();

    private int totalHits;

    @Override
    public void start() {
        vertx.eventBus().consumer(Events.INITIAL_PAGE, message -> {
            List<String> urlsToCrawl = new LinkedList<>();
            urlsToCrawl.add(message.body().toString());

            crawl(urlsToCrawl, () -> {
                pages.values().forEach(page -> page.setPriority((double) page.getHits() / totalHits));
                message.reply(new JsonArray(new ArrayList<>(pages.values())));
            });
        });
    }

    private void crawl(List<String> urlsToCrawl, Runnable callback) {
        if (urlsToCrawl.size() > 0) {
            List<Future> futures = new LinkedList<>();

            urlsToCrawl.forEach(url -> {
                Future<Message<PageInfo>> future = Future.future();
                vertx.eventBus().send(Events.CRAWL_PAGE, url, future.completer());
                futures.add(future);
            });

            CompositeFuture.join(futures).setHandler(futureEvent -> {
                CompositeFuture results = futureEvent.result();
                for (int i = 0; i < results.size(); i++) {
                    if (results.succeeded(i)) {
                        Message<PageInfo> message = results.resultAt(i);
                        PageInfo pageInfo = new PageInfo(message.body()).setHits(1);
                        totalHits++;

                        pages.put(pageInfo.getUrl(), pageInfo);

                        crawlChildren(pageInfo, callback);
                    }
                }
            });
        } else {
            callback.run();
        }
    }

    private void crawlChildren(PageInfo pageInfo, Runnable callback) {
        List<String> childrenToCrawl = new LinkedList<>();

        pageInfo.getChildren().forEach(childUrl -> {
            if (pages.containsKey(childUrl)) {
                PageInfo childPage = pages.get(childUrl);
                childPage.setHits(childPage.getHits() + 1);
                totalHits++;
            } else {
                childrenToCrawl.add(childUrl);
            }
        });

        crawl(childrenToCrawl, callback);
    }
}
