package com.oguzdev.trendinghacker.wearable.bg;

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
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;
import com.oguzdev.trendinghacker.common.util.Constants;
import com.oguzdev.trendinghacker.common.util.NotificationUtils;
import com.oguzdev.trendinghacker.wearable.R;

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
        Log.v(TAG, "NotificationUpdateService onCreate");
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "NotificationUpdateService onDataChanged");
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
                                R.drawable.background2,
                                R.mipmap.ic_launcher,
                                R.drawable.ic_instapaper,
                                R.string.action_save,
                                R.drawable.ic_phone_android_white_48dp,
                                R.string.action_browser,
                                NotificationActionTransmitterService.class);
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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());
            Log.v("myTag", "Message path received on watch is: " + messageEvent.getPath());
            Log.v("myTag", "Message received on watch is: " + message);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}