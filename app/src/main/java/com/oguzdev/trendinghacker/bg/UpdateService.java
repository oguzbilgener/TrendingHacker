package com.oguzdev.trendinghacker.bg;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class UpdateService extends Service {

    public static final int ALARM_CODE = 991917;

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
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        if(!wl.isHeld()) {
            wl.acquire();
        }

        try {

            return START_STICKY;
        }
        finally {
            if(wl.isHeld()) {
                wl.release();
            }
        }
    }

    public UpdatePrefs getUpdatePrefs(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        UpdatePrefs prefs = null;
        if(sp.contains(UpdatePrefs.kEnabled) && sp.contains(UpdatePrefs.kLastUpdate)
                && sp.contains(UpdatePrefs.kRecentlyDisplayedItems)) {
            Long[] recentlyDisplayedItems = new Gson().fromJson(sp.getString(
                    UpdatePrefs.kRecentlyDisplayedItems, ""), Long[].class);
            prefs = new UpdatePrefs(sp.getBoolean(UpdatePrefs.kEnabled, false),
                    sp.getLong(UpdatePrefs.kLastUpdate, 0), recentlyDisplayedItems);
        }
        return prefs;
    }

    public void storeUpdatePrefs(UpdatePrefs prefs, Context context) {
        if(prefs == null) {
            throw new IllegalArgumentException("no prefs to store");
        }
        String recentlyDisplayedStr = new Gson().toJson(prefs.recentlyDisplayedItems);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(UpdatePrefs.kEnabled, prefs.enabled)
                 .putLong(UpdatePrefs.kLastUpdate, prefs.lastUpdate)
                 .putString(UpdatePrefs.kRecentlyDisplayedItems, recentlyDisplayedStr).apply();
    }

    public static class UpdatePrefs {
        public static final String kEnabled = "update_enabled";
        public static final String kLastUpdate = "last_update";
        public static final String kRecentlyDisplayedItems = "recently_displayed_items";
        public Boolean enabled;
        public Long lastUpdate;
        public Long[] recentlyDisplayedItems;

        public UpdatePrefs(Boolean enabled, Long lastUpdate, Long[] recentlyDisplayedItems) {
            this.enabled = enabled;
            this.lastUpdate = lastUpdate;
            this.recentlyDisplayedItems = recentlyDisplayedItems;
        }
    }
}
