package com.teodorstoev.sitemapbuddy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.teodorstoev.sitemapbuddy.components.PageCrawler;
import com.teodorstoev.sitemapbuddy.components.SiteMapper;
import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.OutputXml;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adandris on 10.05.17.
 */
public class EntryPoint {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new PageCrawler(), onPageCrawlerDeploy -> {
            vertx.deployVerticle(new SiteMapper(), onSiteMapperDeploy -> {
                vertx.eventBus().send(Events.INITIAL_PAGE, "http://www.example.com", event -> {
                    if (event.succeeded()) {
                        JsonArray result = (JsonArray) event.result().body();
                        List<PageInfo> urlSet = new ArrayList<>(result.size());
                        result.forEach(o -> urlSet.add(new PageInfo((JsonObject) o)));

                        OutputXml outputXml = new OutputXml();
                        outputXml.setUrlSet(urlSet.toArray());

                        XmlMapper mapper = new XmlMapper();
                        mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
                        mapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
                        try {
                            String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputXml);
                            System.out.println(output);

                            vertx.close();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
        });
    }
}
