package com.url.shortener;


import org.springframework.data.annotation.Id;

public class Url {

    @Id
    private String id;

    private String sourceUrl;
    private String shortcut;

    public Url(String id, String sourceUrl, String shortcut) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.shortcut = shortcut;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }
}
