package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Created by adandris on 10.05.17.
 */
public class PageCrawler extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer(Events.CRAWL_PAGE, message -> {
            message.reply(new PageInfo(new JsonObject().put("infoFor", message.body())));
        });
    }
}
