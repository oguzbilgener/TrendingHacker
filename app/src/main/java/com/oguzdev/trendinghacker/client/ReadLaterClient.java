package com.oguzdev.trendinghacker.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class ReadLaterClient {

    public static void save(final String url, final Credentials credentials, Context context,
                            final SaveResultListener listener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String apiUrl = "https://www.instapaper.com/api/add";
        StringRequest saveRequest = new StringRequest(Request.Method.POST, apiUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    listener.success(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.failure(error.toString());
                }
            }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", credentials.username);
                params.put("password", credentials.password);
                params.put("url", url);
                return params;
            }
        };
    queue.add(saveRequest);
    }

    public static interface SaveResultListener {
        public void success(String response);
        public void failure(String reason);
    }

    public static Credentials getStoredCredentials(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Credentials storedCredentials = null;
        if(sp.contains(Credentials.kUsername) && sp.contains(Credentials.kPassword)) {
            storedCredentials = new Credentials(sp.getString(Credentials.kUsername,""),
                                                sp.getString(Credentials.kPassword, ""));
        }
        return storedCredentials;
    }

    public static void storeCredentials(Credentials credentials, Context context) {
        if(credentials == null) {
            throw new IllegalArgumentException("no credentials to store");
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Credentials.kUsername, credentials.username)
                 .putString(Credentials.kPassword, credentials.password).apply();
    }

    public static class Credentials {
        public static final String kUsername = "readlater_username";
        public static final String kPassword = "readlater_password";
        public String username;
        public String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
