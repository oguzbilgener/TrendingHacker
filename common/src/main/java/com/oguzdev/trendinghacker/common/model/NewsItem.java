package com.oguzdev.trendinghacker.common.model;

import java.io.Serializable;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class NewsItem implements Serializable {
    public Long id;
    public Long time;
    public String title;
    public String url;
    public int score;

    public NewsItem(Long id, Long time, String title, String url, int score) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.url = url;
        this.score = score;
    }

    public NewsItem() {
        this.id = 0L;
        this.time = 0L;
        this.title = "";
        this.url = "";
        this.score = 0;
    }

    public String toString() {
        return title;
    }
}
