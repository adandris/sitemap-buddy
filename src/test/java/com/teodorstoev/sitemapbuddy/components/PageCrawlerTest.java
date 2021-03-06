package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by adandris on 14.05.17.
 */
@RunWith(VertxUnitRunner.class)
public class PageCrawlerTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp() {
        DeploymentOptions pageCrawlerConfig = new DeploymentOptions().setConfig(new JsonObject().put("concurrency", 1));

        rule.vertx().deployVerticle(new PageCrawler(), pageCrawlerConfig);
    }

    @Test
    public void testSomething(TestContext context) {
        Vertx vertx = rule.vertx();

        Async async = context.async();
        vertx.eventBus().send(Events.CRAWL_PAGE, "https://www.teodorstoev.com", event -> {
            if (event.succeeded()) {
                JsonObject result = new JsonObject(event.result().body().toString());

                context.assertEquals("https://www.teodorstoev.com", result.getString("url"));
                context.assertNotNull(result.getString("lastModified"));
                context.assertTrue(result.getString("lastModified").endsWith("Z"));
                context.assertEquals(9, result.getJsonArray("children").size());

                async.complete();
            } else {
                context.fail();
            }
        });
    }
}
