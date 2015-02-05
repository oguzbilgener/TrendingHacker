package com.oguzdev.trendinghacker.bg;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.oguzdev.trendinghacker.R;
import com.oguzdev.trendinghacker.client.HNClient;
import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;
import com.oguzdev.trendinghacker.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UpdateService extends Service {

    final static String GROUP_KEY = "group_key_trending_news";

    public static final int UPDATE_TIMEOUT = 90; // seconds
    public static final int MIN_UPDATE_INTERVAL = 30; // minutes

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        if(!wl.isHeld()) {
            wl.acquire();
        }
        Log.i("oguz", "UpdateService startCommand");

        // Set a timeout to release the wake lock just in case something goes bad.
        final Handler timeoutHandler = new Handler();
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(wl.isHeld()) {
                    wl.release();
                }
            }
        }, UPDATE_TIMEOUT);

        UpdatePrefs prefs = UpdatePrefs.getUpdatePrefs(this);
        if(prefs == null) {
            prefs = new UpdatePrefs();
        }

        if(prefs.enabled && (System.currentTimeMillis() -
                prefs.lastUpdate >= MIN_UPDATE_INTERVAL * 60 * 1000)) {
            HNClient hn = new HNClient(this, new TrendingResults(prefs, wl, this));
            hn.beginRetrieveTrending();
        }
        else {
            if(wl.isHeld()) {
                wl.release();
            }
        }
        return START_NOT_STICKY;
    }

    private class TrendingResults implements HNClient.RetrieveTrendingListener {
        private UpdatePrefs prefs;
        private PowerManager.WakeLock wl;
        private Context context;

        public TrendingResults(UpdatePrefs prefs, PowerManager.WakeLock wl, Context context) {
            this.prefs = prefs;
            this.wl = wl;
            this.context = context;
        }

        @Override
        public void onRetrieve(NewsItem[] items) {
            if(items != null) {
                Log.d("oguz", "retrieved news items with length " + items.length);
                List<NewsItem> newItems = new ArrayList<>();
                for(int i=0;i<items.length;i++) {
                    if(items[i] != null) {
                        newItems.add(items[i]);
                    }
                }
                if(prefs != null) {
                    if(prefs.recentlyDisplayedItems != null) {
                        for (int i = 0; i < prefs.recentlyDisplayedItems.length &&
                                newItems.size() > 0; i++) {
                            for(int j = 0; j < newItems.size(); j++) {
                                if(prefs.recentlyDisplayedItems[i].id.equals(newItems.get(j).id)) {
                                    newItems.remove(j);
                                }
                            }
                        }
                    }
                    if(newItems.size() > 3) {
                        newItems = newItems.subList(0, 3);
                    }

                    NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(context);

                    for (int i = 0; i < prefs.recentlyDisplayedItems.length; i++) {
                        notificationManager.cancel((int)(prefs.recentlyDisplayedItems[i].id % 100000000));
                    }

                    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();

                    NotificationCompat.WearableExtender wearableExtender =
                            new NotificationCompat.WearableExtender()
                                    .setHintHideIcon(true)
                                    .setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.contemporary_china));



                    for(NewsItem item: newItems) {
                        Intent saveIntent = new Intent(Intent.ACTION_VIEW);
                        PendingIntent savePendingIntent =
                                PendingIntent.getActivity(context, 0, saveIntent, 0);

                        String descriptionText = "";
                        try {
                            descriptionText = StringUtils.getDomainName(item.url);
                        }
                        catch(Exception e) {}

                        Notification notification = new NotificationCompat.Builder(context)
                                                           .setSmallIcon(R.mipmap.ic_launcher)
                                                           .extend(wearableExtender)
                                                           .setContentTitle(descriptionText)
                                                           .setContentText(item.title)
                                                           .addAction(android.R.drawable.ic_input_add, getString(R.string.action_save), savePendingIntent)
                                                           .build();
                        notificationManager.notify((int) (item.id % 100000000), notification);
                    }


                    prefs.insertRecentlyDisplayed(newItems);
                    prefs.storeUpdatePrefs(context);
                }
            }
            if(wl != null && wl.isHeld()) {
                wl.release();
            }
        }
    }
}
