package com.oguzdev.trendinghacker.common.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.List;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class UpdatePrefs {
    public static final String kEnabled = "update_enabled";
    public static final String kLastUpdate = "last_update";
    public static final String kRecentlyDisplayedItems = "recently_displayed_items";
    public Boolean enabled;
    public Long lastUpdate;
    public NewsItem[] recentlyDisplayedItems;

    public UpdatePrefs(Boolean enabled, Long lastUpdate, NewsItem[] recentlyDisplayedItems) {
        this.enabled = enabled;
        this.lastUpdate = lastUpdate;
        this.recentlyDisplayedItems = recentlyDisplayedItems;
    }

    public UpdatePrefs() {
        this.enabled = false;
        this.lastUpdate = 0L;
        this.recentlyDisplayedItems = new NewsItem[0];
    }

    public void insertRecentlyDisplayed(List<NewsItem> newItems) {
        int oldMaxSize = recentlyDisplayedItems.length < 50 ? recentlyDisplayedItems.length : 50;
        NewsItem[] updated = new NewsItem[oldMaxSize + newItems.size()];
        for(int i = 0; i < newItems.size(); i++) {
            updated[i] = newItems.get(i);
        }
        for(int i = 0; i < oldMaxSize; i++) {
            updated[i + newItems.size()] = recentlyDisplayedItems[i];
        }
        this.recentlyDisplayedItems = updated;
    }

    public static UpdatePrefs getUpdatePrefs(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        UpdatePrefs prefs = null;
        if(sp.contains(UpdatePrefs.kEnabled) && sp.contains(UpdatePrefs.kLastUpdate)
                && sp.contains(UpdatePrefs.kRecentlyDisplayedItems)) {
            NewsItem[] recentlyDisplayedItems = new Gson().fromJson(sp.getString(
                    UpdatePrefs.kRecentlyDisplayedItems, ""), NewsItem[].class);
            prefs = new UpdatePrefs(sp.getBoolean(UpdatePrefs.kEnabled, false),
                    sp.getLong(UpdatePrefs.kLastUpdate, 0), recentlyDisplayedItems);
        }
        return prefs;
    }

    public void storeUpdatePrefs(Context context) {
        String recentlyDisplayedStr = new Gson().toJson(recentlyDisplayedItems);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(UpdatePrefs.kEnabled, enabled)
                .putLong(UpdatePrefs.kLastUpdate, lastUpdate)
                .putString(UpdatePrefs.kRecentlyDisplayedItems, recentlyDisplayedStr).apply();
    }
}