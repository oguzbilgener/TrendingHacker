package com.oguzdev.trendinghacker.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oguzdev.trendinghacker.bg.UpdateReceiver;

import java.util.Calendar;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class AlarmUtils {

    public static final int HOURLY_ALARM_ID = 463681;
    private static final boolean DEBUG = true;

    public static void setupHourlyAlarm(Context context) {

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, HOURLY_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm to start at next o'clock
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(!DEBUG) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 0);
        }
        else {
            calendar.add(Calendar.MINUTE, 1);
        }

        int interval = 1000 * 60 * 60;
        if(DEBUG) {
            interval = 1000 * 60;
        }

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                interval, alarmIntent);

        Log.i("oguz", "Alarm set for every "+(interval/1000)+" seconds after " + calendar.getTime().toString());
    }

    public static void cancelHourlyAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, HOURLY_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(alarmIntent);
        Log.i("oguz", "hourly alarm cancelled");
    }
}
