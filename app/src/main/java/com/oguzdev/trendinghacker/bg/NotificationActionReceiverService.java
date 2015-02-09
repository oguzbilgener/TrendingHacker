package com.oguzdev.trendinghacker.bg;

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
import com.oguzdev.trendinghacker.client.ReadLaterClient;
import com.oguzdev.trendinghacker.common.util.Constants;

import static com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME;

public class NotificationActionReceiverService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DeleteDataItemsResult> {

    private static final String TAG = "oguz";
    private GoogleApiClient mGoogleApiClient;

    public NotificationActionReceiverService() {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "NotificationActionReceiverService onDataChanged");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "NotificationActionReceiverService found changed");
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                if(Constants.URL_PATH.equals(dataEvent.getDataItem().getUri().getPath())) {
                    Log.d(TAG, "NotificationActionReceiverService found url path");
                    String url = dataMap.getString(Constants.URL_DATA);
                    String action = dataMap.getString(Constants.URL_ACTION);
                    if(Constants.ACTION_READ_LATER_W.equals(action)) {
                        readLater(url);
                    }
                    else if(Constants.ACTION_OPEN_IN_BROWSER_W.equals(action)) {
                        openInBrowser(url);
                    }
                }
            }
        }
    }

    private void openInBrowser(String url) {
        Log.i(TAG, "openInBrowser "+url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(browserIntent);
    }

    private void readLater(String url) {
        Log.i(TAG, "readLater "+url);
        ReadLaterClient.Credentials credentials = ReadLaterClient.getStoredCredentials(this);
        if(credentials != null) {
            ReadLaterClient.save(url, credentials, this, new ReadLaterClient.SaveResultListener() {
                @Override
                public void success(String response) {
                    Log.d(TAG, "saved url!");
                }

                @Override
                public void failure(String reason) {
                    Log.w(TAG, "Save failed because: "+reason);
                    // TODO: Show a notification on the phone
                }
            });
        }
        else {
            Log.w(TAG, "ReadLater Credentials null. not saving.");
        }
    }

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
