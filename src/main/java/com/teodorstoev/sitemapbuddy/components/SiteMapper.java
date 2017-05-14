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
    private Queue<String> urlsToCrawl = new LinkedList<>();

    private Map<String, PageInfo> pages = new HashMap<>();

    @Override
    public void start() {
        vertx.eventBus().consumer(Events.INITIAL_PAGE, message -> {
            urlsToCrawl.add(message.body().toString());

            crawl(() -> message.reply(new JsonArray(new ArrayList<>(pages.values()))));
        });
    }

    private void crawl(Runnable callback) {
        if (urlsToCrawl.size() > 0) {
            List<Future> futures = new LinkedList<>();

            urlsToCrawl.forEach(url -> {
                Future<Message<PageInfo>> future = Future.future();
                vertx.eventBus().send(Events.CRAWL_PAGE, url, future.completer());
                futures.add(future);
            });
            urlsToCrawl.clear();

            CompositeFuture.join(futures).setHandler(futureEvent -> {
                CompositeFuture results = futureEvent.result();
                for (int i = 0; i < results.size(); i++) {
                    if (results.succeeded(i)) {
                        Message<PageInfo> message = results.resultAt(i);
                        PageInfo pageInfo = new PageInfo(message.body());

                        pages.put(pageInfo.getUrl(), pageInfo);

                        pageInfo.getChildren().forEach(childUrl -> {
                            if (pages.containsKey(childUrl)) {
                                pages.get(childUrl).setHits(pages.get(childUrl).getHits() + 1);
                            } else {
                                urlsToCrawl.add(childUrl);
                            }
                        });
                    }
                }
                crawl(callback);
            });
        } else {
            callback.run();
        }
    }
}
