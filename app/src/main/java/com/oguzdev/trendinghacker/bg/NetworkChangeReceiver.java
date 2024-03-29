package com.oguzdev.trendinghacker.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("oguz", "Network State Changed! " + (new Date()));
        Intent passIntent = new Intent(context, UpdateService.class);
        // pass all the extras
        passIntent.putExtras(intent);
        context.startService(passIntent);
    }
}