package com.teodorstoev.sitemapbuddy.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Created by adandris on 14.05.17.
 */
@JacksonXmlRootElement(localName = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class OutputXml {
    @JacksonXmlProperty(localName = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    @JacksonXmlElementWrapper(useWrapping = false)
    private Object[] urlSet;

    public Object[] getUrlSet() {
        return urlSet;
    }

    public void setUrlSet(Object[] urlSet) {
        this.urlSet = urlSet;
    }
}
