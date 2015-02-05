package com.oguzdev.trendinghacker.model;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class NewsItem {
    public Long id;
    public Long time;
    public String title;
    public String url;

    public NewsItem(Long id, Long time, String title, String url) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.url = url;
    }

    public NewsItem() {
        this.id = 0L;
        this.time = 0L;
        this.title = "";
        this.url = "";
    }

    public String toString() {
        return title;
    }
}
