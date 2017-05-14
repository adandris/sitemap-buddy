package com.teodorstoev.sitemapbuddy.domain;

import io.vertx.core.json.JsonObject;

/**
 * Created by adandris on 14.05.17.
 */
public class PageInfo extends JsonObject {
    public PageInfo() {

    }

    public PageInfo(final JsonObject source) {
        super(source.getMap());
    }
}
