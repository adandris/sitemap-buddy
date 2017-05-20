package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
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
import java.time.format.DateTimeParseException;

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

                        parseResponseAndReply(message, baseUrl, response);
                    }
                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    private void parseResponseAndReply(Message<Object> message, String baseUrl, Connection.Response response) {
        try {
            Document document = response.parse();

            Elements links = document.select("a");

            JsonArray children = links
                    .stream()
                    .map(element -> element.absUrl("href"))
                    .filter(href -> href.matches("https?://" + baseUrl + ".*"))
                    .collect(JsonArray::new, JsonArray::add, JsonArray::add);

            PageInfo pageInfo = new PageInfo()
                    .setUrl(message.body().toString())
                    .setChildren(children);

            String lastModifiedHeader = response.header("Last-Modified");
            if (lastModifiedHeader != null) {
                try {
                    LocalDateTime lastModified = LocalDateTime.parse(lastModifiedHeader, DateTimeFormatter.RFC_1123_DATE_TIME);

                    pageInfo.setLastModified(lastModified.toString());
                } catch (DateTimeParseException e) {
                    System.out.println("Failed to parse date '" + lastModifiedHeader + "'");
                }
            }

            message.reply(pageInfo);
        } catch (IOException e) {
            message.fail(1, e.getMessage());
        }
    }
}