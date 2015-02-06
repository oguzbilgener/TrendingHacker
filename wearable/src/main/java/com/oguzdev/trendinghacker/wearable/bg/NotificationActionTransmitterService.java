package com.oguzdev.trendinghacker.wearable.bg;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.oguzdev.trendinghacker.common.util.Constants;

import java.util.ArrayList;

public class NotificationActionTransmitterService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String url;
    private String action;

    public NotificationActionTransmitterService() {
    }

    @Override
    public void onCreate() {
        Log.d("oguz", "NotificationActionTransmitterService onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (Constants.ACTION_READ_LATER.equals(action) || Constants.ACTION_OPEN_IN_BROWSER.equals(action)) {
                Log.d("oguz", "NotificationActionTransmitterService has action");
                String url = intent.getExtras().getString(Constants.URL_DATA);
                if(url != null) {
                    Log.d("oguz", "NotificationActionTransmitterService has url");
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Wearable.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    mGoogleApiClient.connect();
                    this.url = url;
                    this.action = action;
                }
                else {
                    finishFailed();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(url != null) {
            Log.d("oguz", "NotificationActionTransmitterService google api connected");
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.URL_PATH);
            putDataMapRequest.getDataMap().putString(Constants.URL_DATA, url);
            putDataMapRequest.getDataMap().putString(Constants.URL_ACTION, action);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e("oguz", "send url to app: Failed to set the data, "
                                        + "status: " + dataItemResult.getStatus().getStatusCode());
                            }
                            else {
                                Log.d("oguz", "url sent to app back, success!");
                            }
                            finishSuccess();
                        }
                    });
        }
        else {
            finishFailed();
        }

        Intent displayIntent = new Intent(this, ConfirmationActivity.class);
        displayIntent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(displayIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopSelf();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        finishFailed();
    }

    public void finishSuccess() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        stopSelf();
    }

    public void finishFailed() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        Intent displayIntent = new Intent(this, ConfirmationActivity.class);
        displayIntent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
        displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(displayIntent);
        stopSelf();
    }
}
