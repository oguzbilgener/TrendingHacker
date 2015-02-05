package com.oguzdev.trendinghacker.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent passIntent = new Intent(context, UpdateService.class);
        // pass all the extras
        passIntent.putExtras(intent);
        context.startService(passIntent);
    }
}