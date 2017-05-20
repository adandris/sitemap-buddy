package com.teodorstoev.sitemapbuddy;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.teodorstoev.sitemapbuddy.components.PageCrawler;
import com.teodorstoev.sitemapbuddy.components.SiteMapper;
import com.teodorstoev.sitemapbuddy.domain.Events;
import com.teodorstoev.sitemapbuddy.domain.OutputXml;
import com.teodorstoev.sitemapbuddy.domain.PageInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by adandris on 10.05.17.
 */
public class EntryPoint {
    private static final String OPTION_URL = "url";

    private static final String OPTION_OUTPUT = "output";

    public static void main(String[] args) {
        CommandLine commandLine = parseCommandLine(args);
        if (commandLine == null) {
            System.exit(1);
        }

        String siteUrl = commandLine.getOptionValue(OPTION_URL);
        String path = commandLine.getOptionValue(OPTION_OUTPUT);

        Vertx vertx = Vertx.vertx();

        deployVerticles(vertx, onDeploy -> createSitemap(vertx, siteUrl, sitemap -> {
            List<PageInfo> urlSet = new ArrayList<>(sitemap.size());
            sitemap.forEach(o -> urlSet.add(new PageInfo((JsonObject) o)));

            urlSet.sort(Comparator.comparingInt(PageInfo::getHits).reversed());

            OutputXml outputXml = new OutputXml();
            outputXml.setUrlSet(urlSet.toArray());

            mapToXml(outputXml, path, vertx::close);
        }));
    }

    private static CommandLine parseCommandLine(String[] args) {
        Options options = new Options();

        Option url = new Option(OPTION_URL.substring(0, 1), OPTION_URL, true, "Site URL");
        url.setRequired(true);
        options.addOption(url);

        Option output = new Option(OPTION_OUTPUT.substring(0, 1), OPTION_OUTPUT, true, "Output file");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("sitemap-buddy", options);
            return null;
        }
    }

    private static void deployVerticles(Vertx vertx, Handler<AsyncResult<String>> onDeploy) {
        vertx.deployVerticle(new PageCrawler(),
                onPageCrawlerDeploy -> vertx.deployVerticle(new SiteMapper(), onDeploy));
    }

    private static void createSitemap(Vertx vertx, String siteUrl, Consumer<JsonArray> consumer) {
        vertx.eventBus().send(Events.INITIAL_PAGE, siteUrl, event -> {
            if (event.succeeded()) {
                JsonArray result = (JsonArray) event.result().body();
                consumer.accept(result);
            }
        });
    }

    private static void mapToXml(OutputXml outputXml, String path, Runnable callback) {
        XmlMapper mapper = new XmlMapper();
        mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        mapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), outputXml);
            callback.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
