package com.teodorstoev.sitemapbuddy.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;

/**
 * Created by adandris on 14.05.17.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class PageInfo extends JsonObject {
    private static final String URL = "url";

    private static final String LAST_MODIFIED = "lastModified";

    private static final String HITS = "hits";

    private static final String PRIORITY = "priority";

    private static final String CHILDREN = "children";

    public PageInfo() {
    }

    public PageInfo(final JsonObject source) {
        super(source.getMap());
    }

    @XmlElement(name = "loc")
    public String getUrl() {
        return getString(URL);
    }

    public PageInfo setUrl(final String url) {
        put(URL, url);
        return this;
    }

    @XmlElement(name = "lastmod")
    public String getLastModified() {
        return getString(LAST_MODIFIED);
    }

    public PageInfo setLastModified(final String lastModified) {
        put(LAST_MODIFIED, lastModified);
        return this;
    }

    @XmlElement(name = "priority")
    public double getPriority() {
        return getDouble(PRIORITY);
    }

    public PageInfo setPriority(double priority) {
        put(PRIORITY, priority);
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
