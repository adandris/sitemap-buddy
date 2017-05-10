package com.teodorstoev.sitemapbuddy;

import com.teodorstoev.sitemapbuddy.components.PageCrawler;
import com.teodorstoev.sitemapbuddy.components.SiteMapper;
import io.vertx.core.Vertx;

/**
 * Created by adandris on 10.05.17.
 */
public class EntryPoint {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new SiteMapper());
        vertx.deployVerticle(new PageCrawler());

        System.out.println("Hello World!");
    }
}
