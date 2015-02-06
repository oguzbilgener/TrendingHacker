package com.oguzdev.trendinghacker.wearable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;
import com.oguzdev.trendinghacker.common.util.Constants;
import com.oguzdev.trendinghacker.common.util.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class NotificationUpdateService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DeleteDataItemsResult> {

    private static final String TAG = "oguz";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (Constants.ACTION_DISMISS.equals(action)) {
                // We need to dismiss the wearable notification. We delete the data item that
                // created the notification and that is how we inform the phone
                ArrayList<Integer> notificationIds = intent.getExtras().getIntegerArrayList(Constants.KEY_NOTIFICATION_ID);


//                dismissPhoneNotification(notificationId);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String content = dataMap.getString(Constants.NEWS_DATA);
                List<NewsItem> newsItems = new Gson().fromJson(content, new TypeToken<List<NewsItem>>(){}.getType());
                if(Constants.NOTIF_PATH.equals(dataEvent.getDataItem().getUri().getPath())) {
                    // Show the notification!
                    String prefsContent = dataMap.getString(Constants.PREFS_DATA);
                    UpdatePrefs prefs = new Gson().fromJson(prefsContent, UpdatePrefs.class);
                    if(prefs != null) {
                        Log.d(TAG, "Wear notifications updated with "+newsItems.size()+" new items.");
                        NotificationUtils.cancelNewsNotifications(this, prefs.recentlyDisplayedItems);
                        NotificationUtils.notifyNews(this, newsItems,
                                R.drawable.contemporary_china,
                                R.mipmap.ic_launcher,
                                android.R.drawable.ic_input_add,
                                R.string.action_save);
                    }
                    else {
                        if (Log.isLoggable(TAG, Log.ERROR)) {
                            Log.e(TAG, "PREFS NULL!!");
                        }
                    }
                }

            } else if (dataEvent.getType() == DataEvent.TYPE_DELETED) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "DataItem deleted: " + dataEvent.getDataItem().getUri().getPath());
                }
//                if (Constants.BOTH_PATH.equals(dataEvent.getDataItem().getUri().getPath())) {
//                    // Dismiss the corresponding notification
//                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
//                            .cancel(Constants.WATCH_ONLY_ID);
//                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        final Uri dataItemUri =
                new Uri.Builder().scheme(WEAR_URI_SCHEME).path(Constants.NOTIF_PATH).build();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected Deleting Uri: " + dataItemUri.toString());
        }
        Wearable.DataApi.deleteDataItems(
                mGoogleApiClient, dataItemUri).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
        if (!deleteDataItemsResult.getStatus().isSuccess()) {
            Log.e(TAG, "dismissWearableNotification(): failed to delete DataItem");
        }
        mGoogleApiClient.disconnect();
    }
}