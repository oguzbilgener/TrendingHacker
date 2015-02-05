package com.oguzdev.trendinghacker.client;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.oguzdev.trendinghacker.model.NewsItem;

import java.util.Map;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class HNClient {

    public static final int RETRIVE_COUNT = 5;
    public static final int RETRIEVE_TIMEOUT = 60; // seconds

    private int foundItemCount;
    private int retrievedItemCount;
    private NewsItem[] retrievedItems;
    private RetrieveTrendingListener listener;

    private Handler timeoutHandler;
    private Runnable timeoutTask;

    public HNClient(Context context, RetrieveTrendingListener listener) {
        Firebase.setAndroidContext(context);
        resetCounts();
        this.listener = listener;
        this.timeoutHandler = new Handler();
        this.timeoutTask = new Runnable() {
            @Override
            public void run() {
                Log.w("oguz", "oh no: timeout happened");
                finalizeImmediately();
            }
        };
    }

    public void beginRetrieveTrending() {
        resetCounts();
        final Firebase firebase = new Firebase("https://hacker-news.firebaseio.com/v0/topstories");
        Query query = firebase.limitToFirst(RETRIVE_COUNT);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                foundItemCount = (int) snapshot.getChildrenCount();
                retrievedItems = new NewsItem[foundItemCount];
                for (int i = 0; i < foundItemCount; i++) {
                    try {
                        Long id = snapshot.child(Integer.toString(i)).getValue(Long.class);
                        retrieveParticularItem(i, id);
                    } catch (Exception e) {
                        retrieveParticularItem(i, -1L);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                finalizeImmediately();
            }
        });
        timeoutHandler.postDelayed(timeoutTask, RETRIEVE_TIMEOUT * 1000);
    }

    public void retrieveParticularItem(final int index, final Long itemId) {
        if(itemId > 0) {
            final Firebase firebase = new Firebase("https://hacker-news.firebaseio.com/v0/");
            firebase.child("item").child(Long.toString(itemId)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        NewsItem item = new NewsItem();
                        Map<String, Object> pairs = (Map<String, Object>) snapshot.getValue();
                        item.id = (Long) pairs.get("id");
                        item.time = (Long) pairs.get("time");
                        item.title = (String) pairs.get("title");
                        item.url = (String) pairs.get("url");
                        if(retrievedItems != null && retrievedItems.length > index) {
                            retrievedItems[index] = item;
                        }
                        incrementRetrievedCount();
                        finalizeIfFinished();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        incrementRetrievedCount();
                        finalizeIfFinished();
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    incrementRetrievedCount();
                    finalizeIfFinished();
                }
            });
        }
        else {
            incrementRetrievedCount();
            finalizeIfFinished();
        }
    }

    private synchronized void finalizeIfFinished() {
        if(foundItemCount != 0 && foundItemCount == retrievedItemCount) {
            timeoutHandler.removeCallbacksAndMessages(null);
            listener.onRetrieve(retrievedItems);
            resetCounts();
        }
    }

    private synchronized void finalizeImmediately() {
        if(listener != null) {
           timeoutHandler.removeCallbacksAndMessages(null);
           listener.onRetrieve(new NewsItem[0]);
            resetCounts();
        }
    }

    private synchronized void incrementRetrievedCount() {
        retrievedItemCount++;
    }

    private synchronized void resetCounts() {
        foundItemCount = 0;
        retrievedItemCount = 0;
    }

    public interface RetrieveTrendingListener {
        public void onRetrieve(NewsItem[] items);
    }
}
