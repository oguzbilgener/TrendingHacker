package com.oguzdev.trendinghacker.bg;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.oguzdev.trendinghacker.R;
import com.oguzdev.trendinghacker.client.HNClient;
import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;
import com.oguzdev.trendinghacker.common.util.Constants;
import com.oguzdev.trendinghacker.common.util.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class UpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final static String TAG = "oguz";

    public static final int UPDATE_TIMEOUT = 90; // seconds
    public static final int MIN_UPDATE_INTERVAL = 30; // minutes

    private GoogleApiClient mGoogleApiClient;
    private Handler timeoutHandler;
    private PowerManager.WakeLock wl;

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
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        if(!wl.isHeld()) {
            wl.acquire();
        }
        Log.i(TAG, "UpdateService startCommand "+this.hashCode());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        return START_NOT_STICKY;
    }

    private void finishService() {
        if(wl != null && wl.isHeld()) {
            wl.release();
        }
        if(timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy UpdateService "+this.hashCode());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "GoogleApi onConnected");
        // Set a timeout to release the wake lock just in case something goes bad.
        timeoutHandler = new Handler();
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishService();
            }
        }, UPDATE_TIMEOUT * 1000);

        UpdatePrefs prefs = UpdatePrefs.getUpdatePrefs(this);
        if(prefs == null) {
            prefs = new UpdatePrefs();
        }

        if(prefs.enabled && (System.currentTimeMillis() -
                prefs.lastUpdate >= MIN_UPDATE_INTERVAL * 60 * 1000)) {
            HNClient hn = new HNClient(this, new TrendingResults(prefs, this));
            hn.beginRetrieveTrending();
        }
        else {
            finishService();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApi onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("oguz", "Failed to connect to Google API Client");
    }

    private class TrendingResults implements HNClient.RetrieveTrendingListener {
        private UpdatePrefs prefs;
        private Context context;

        public TrendingResults(UpdatePrefs prefs, Context context) {
            this.prefs = prefs;
            this.context = context;
        }

        @Override
        public void onRetrieve(NewsItem[] items) {
            if(items != null) {
                Log.d(TAG, "retrieved news items with length " + items.length);
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

                    prefs.insertRecentlyDisplayed(newItems);
                    prefs.storeUpdatePrefs(context);

                    if (mGoogleApiClient.isConnected()) {
                        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.NOTIF_PATH);
                        putDataMapRequest.getDataMap().putString(Constants.NEWS_DATA, new Gson().toJson(newItems));
                        putDataMapRequest.getDataMap().putString(Constants.PREFS_DATA, new Gson().toJson(prefs));
                        PutDataRequest request = putDataMapRequest.asPutDataRequest();
                        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {
                                        if (!dataItemResult.getStatus().isSuccess()) {
                                            Log.e(TAG, "buildWatchOnlyNotification(): Failed to set the data, "
                                                    + "status: " + dataItemResult.getStatus().getStatusCode());
                                        }
                                        else {
                                            Log.d(TAG, "notif sent to wear, success!");
                                        }
                                        finishService();
                                    }
                                });
                    } else {
                        Log.e(TAG, "buildWearableOnlyNotification(): no Google API Client connection");
                        finishService();
                    }
                }
                else {
                    finishService();
                }
            }
            else {
                finishService();
            }
        }
    }
}
