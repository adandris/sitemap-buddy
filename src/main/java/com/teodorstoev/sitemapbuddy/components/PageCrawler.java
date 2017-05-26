package com.teodorstoev.sitemapbuddy.components;

import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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
            System.out.println("Crawling " + urlToCrawl + " ...");
            try {
                URI uri = new URI(urlToCrawl);
                String baseUrl = uri.getHost();

                executeRequest(uri, event -> {
                    if (event.succeeded()) {
                        Connection.Response response = event.result();

                        parseResponseAndReply(message, baseUrl, response);
                    } else {
                        event.cause().printStackTrace();
                        message.fail(1, event.cause().getMessage());
                    }
                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    private void executeRequest(URI uri, Handler<AsyncResult<Connection.Response>> resultHandler) {
        vertx.executeBlocking(event -> {
            try {
                String decodedUrl = URLDecoder.decode(uri.toString(), "UTF-8");
                Connection.Response response = Jsoup.connect(decodedUrl).execute();
                event.complete(response);
            } catch (Exception e) {
                event.fail(e);
            }
        }, false, resultHandler);
    }

    private void parseResponseAndReply(Message<Object> message, String baseUrl, Connection.Response response) {
        try {
            Document document = response.parse();

            Elements links = document.select("a");

            JsonArray children = links
                    .stream()
                    .map(element -> element.absUrl("href"))
                    .distinct()
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
            System.out.println("Failed to parse page...");
            message.fail(1, e.getMessage());
        }
    }
}