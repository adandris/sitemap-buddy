package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;

import java.time.LocalDateTime;

/**
 * Created by adandris on 10.05.17.
 */
public class PageCrawler extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer(Events.CRAWL_PAGE, message -> {
            PageInfo pageInfo = new PageInfo()
                    .setUrl(message.body().toString())
                    .setLastModified(LocalDateTime.now().toString());

            message.reply(pageInfo);
        });
    }
}
