package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by adandris on 10.05.17.
 */
public class PageCrawler extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer(Events.CRAWL_PAGE, message -> {
            String urlToCrawl = message.body().toString();
            try {
                URI uri = new URI(urlToCrawl);
                String baseUrl = uri.getHost();

                vertx.executeBlocking(event -> {
                    try {
                        Connection.Response response = Jsoup.connect(urlToCrawl).execute();
                        event.complete(response);
                    } catch (IOException e) {
                        event.fail(e);
                    }
                }, false, event -> {
                    if (event.succeeded()) {
                        Connection.Response response = (Connection.Response) event.result();
                        LocalDateTime lastModified = LocalDateTime.parse(response.header("Last-Modified"),
                                DateTimeFormatter.RFC_1123_DATE_TIME);

                        try {
                            Document document = response.parse();

                            Elements links = document.select("a[href^=" + baseUrl + "]");

                            JsonArray children = links
                                    .stream()
                                    .map(element -> element.attr("abs:href"))
                                    .collect(JsonArray::new, JsonArray::add, JsonArray::add);

                            PageInfo pageInfo = new PageInfo()
                                    .setUrl(message.body().toString())
                                    .setLastModified(lastModified.toString())
                                    .setChildren(children);

                            message.reply(pageInfo);
                        } catch (IOException e) {
                            message.fail(1, e.getMessage());
                        }
                    }
                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }
}