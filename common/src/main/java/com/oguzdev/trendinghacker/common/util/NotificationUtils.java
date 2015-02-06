package com.oguzdev.trendinghacker.common.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;

import java.util.List;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class NotificationUtils {
    public static final void notifyNews(Context context, List<NewsItem> items,
                                        int backgroundDrawableResource,
                                        int hintIconResource,
                                        int saveIconResource,
                                        int saveStringResource) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true)
                        .setBackground(BitmapFactory.decodeResource(context.getResources(), backgroundDrawableResource));



        for(NewsItem item: items) {
            Intent saveIntent = new Intent(Intent.ACTION_VIEW);
            PendingIntent savePendingIntent =
                    PendingIntent.getActivity(context, 0, saveIntent, 0);

            String descriptionText = "";
            try {
                descriptionText = StringUtils.getDomainName(item.url);
            }
            catch(Exception e) {}

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(hintIconResource)
                    .extend(wearableExtender)
                    .setContentTitle(descriptionText)
                    .setContentText(item.title)
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
