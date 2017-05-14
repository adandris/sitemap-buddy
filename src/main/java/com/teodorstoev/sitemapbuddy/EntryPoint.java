package com.teodorstoev.sitemapbuddy;

import com.teodorstoev.sitemapbuddy.components.PageCrawler;
import com.teodorstoev.sitemapbuddy.components.SiteMapper;
import com.teodorstoev.sitemapbuddy.domain.Events;
import io.vertx.core.Vertx;

/**
 * Created by adandris on 10.05.17.
 */
public class EntryPoint {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new PageCrawler(), onPageCrawlerDeploy -> {
            vertx.deployVerticle(new SiteMapper(), onSiteMapperDeploy -> {
                vertx.eventBus().send(Events.INITIAL_PAGE, "http://www.example.com");
            });
        });
    }
}
