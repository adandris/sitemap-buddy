package com.teodordtoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.components.SiteMapper;
import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.Vertx;
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
public class SiteMapperTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp() {
        Vertx vertx = rule.vertx();

        vertx.deployVerticle(new SiteMapper());

        PageInfo testPageInfo = new PageInfo().setUrl("http://www.example.com");
        vertx.eventBus().consumer(Events.CRAWL_PAGE, message -> message.reply(testPageInfo));
    }

    @Test
    public void testSomething(TestContext context) {
        Vertx vertx = rule.vertx();

        Async async = context.async();
        vertx.eventBus().send(Events.INITIAL_PAGE, "http://www.example.com", event -> {
            if (event.succeeded()) {
                context.assertEquals("[{\"url\":\"http://www.example.com\"}]", event.result().body().toString());

                async.complete();
            } else {
                context.fail();
            }
        });
    }
}
