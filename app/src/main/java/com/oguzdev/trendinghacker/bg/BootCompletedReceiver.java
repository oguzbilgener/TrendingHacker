package com.oguzdev.trendinghacker.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.oguzdev.trendinghacker.util.AlarmUtils;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.w("oguz", "bootCompleted! start service in 30 secs");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmUtils.setupHourlyAlarm(context);
                Log.d("oguz", "starting service 30 secs after boot completed");
                Intent passIntent = new Intent(context, UpdateService.class);
                // pass all the extras
                passIntent.putExtras(intent);
                context.startService(passIntent);
            }
        }, 30 * 1000);
    }

}
