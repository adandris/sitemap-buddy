package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Created by adandris on 10.05.17.
 */
public class SiteMapper extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer(Events.INITIAL_PAGE, message -> {
            vertx.eventBus().send(Events.CRAWL_PAGE, message.body(), event -> {
                if(event.succeeded()) {
                    PageInfo pageInfo = new PageInfo((JsonObject) event.result().body());

                    message.reply(pageInfo.toString());
                }
            });
        });
    }
}
