package com.oguzdev.trendinghacker.common.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.ConfirmationActivity;

import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;

import java.util.List;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class NotificationUtils {

    final static String GROUP_KEY = "group_key_trending_news";

    public static final void notifyNews(Context context, List<NewsItem> items,
                                        int backgroundDrawableResource,
                                        int hintIconResource,
                                        int saveIconResource,
                                        int saveStringResource,
                                        int browserIconResource,
                                        int browserStringResource,
                                        Class urlActionTransmitterServiceName) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();

        Bitmap background = BitmapFactory.decodeResource(context.getResources(), backgroundDrawableResource);

        for(NewsItem item: items) {
            Intent saveIntent = new Intent(context, urlActionTransmitterServiceName);
            saveIntent.setAction(Constants.ACTION_READ_LATER_W);
            saveIntent.putExtra(Constants.URL_ACTION, Constants.ACTION_READ_LATER);
            saveIntent.putExtra(Constants.URL_DATA, item.url);
            saveIntent.putExtra("some_arbitrary_int", (int)(Math.random() * 100000));
            PendingIntent savePendingIntent =
                    PendingIntent.getService(context, 0, saveIntent, 0);

            Intent browserIntent = new Intent(context, urlActionTransmitterServiceName);
            browserIntent.setAction(Constants.ACTION_OPEN_IN_BROWSER_W);
            browserIntent.putExtra(Constants.URL_ACTION, Constants.ACTION_OPEN_IN_BROWSER);
            browserIntent.putExtra(Constants.URL_DATA, item.url);
            browserIntent.putExtra("some_arbitrary_int", (int)(Math.random() * 100000));
            PendingIntent browserPendingIntent =
                    PendingIntent.getService(context, 0, browserIntent, 0);

            String descriptionText = "";
            try {
                descriptionText = StringUtils.getDomainName(item.url);
            }
            catch(Exception e) {}

            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                            .setHintHideIcon(false)
                            .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_LARGE)
                            .setContentIntentAvailableOffline(false)
                            .setBackground(background);

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(hintIconResource)
                    .extend(wearableExtender)
                    .setContentTitle(descriptionText)
                    .setContentText(item.title)
//                    .setContentIntent(browserPendingIntent)
                    .setGroup(GROUP_KEY)
                    .addAction(browserIconResource, context.getString(browserStringResource), browserPendingIntent)
                    .addAction(saveIconResource, context.getString(saveStringResource), savePendingIntent)
                    .build();
            notificationManager.notify((int) (item.id % 100000000), notification);
        }
    }

    public static void cancelNewsNotifications(Context context, NewsItem[] items) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        for (int i = 0; i < items.length; i++) {
            notificationManager.cancel((int)(items[i].id % 100000000));
        }
    }
}
