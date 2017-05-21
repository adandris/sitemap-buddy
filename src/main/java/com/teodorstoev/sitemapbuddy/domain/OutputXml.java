package com.teodorstoev.sitemapbuddy.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by adandris on 14.05.17.
 */
@XmlRootElement(name = "urlset")
@XmlAccessorType(XmlAccessType.FIELD)
public class OutputXml {
    @XmlElement(name = "url")
    private List<PageInfo> urlSet;

    public List<PageInfo> getUrlSet() {
        return urlSet;
    }

    public void setUrlSet(List<PageInfo> urlSet) {
        this.urlSet = urlSet;
    }
}
