package com.teodorstoev.sitemapbuddy.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by adandris on 14.05.17.
 */
public class PageInfo extends JsonObject {
    private static final String URL = "url";

    private static final String LAST_MODIFIED = "lastModified";

    private static final String HITS = "hits";

    private static final String CHILDREN = "children";

    public PageInfo() {
    }

    public PageInfo(final JsonObject source) {
        super(source.getMap());
    }

    @JacksonXmlProperty(localName = "loc", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    public String getUrl() {
        return getString(URL);
    }

    public PageInfo setUrl(final String url) {
        put(URL, url);
        return this;
    }

    @JacksonXmlProperty(localName = "lastmod", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    public String getLastModified() {
        return getString(LAST_MODIFIED);
    }

    public PageInfo setLastModified(final String lastModified) {
        put(LAST_MODIFIED, lastModified);
        return this;
    }

    public int getHits() {
        return getInteger(HITS);
    }

    public PageInfo setHits(int hits) {
        put(HITS, hits);
        return this;
    }

    public List<String> getChildren() {
        JsonArray children = getJsonArray(CHILDREN);
        if (children != null) {
            //noinspection unchecked
            return children.getList();
        }
        return Collections.emptyList();
    }

    public PageInfo setChildren(final JsonArray children) {
        put(CHILDREN, children);
        return this;
    }
}
