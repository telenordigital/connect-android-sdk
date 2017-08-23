package com.telenor.connect;

import android.content.Context;

import com.google.gson.Gson;

import static com.telenor.connect.id.ConnectStore.PREFERENCES_FILE;

public class WellKnownConfigStore {

    private static final String PREFERENCE_KEY_WELL_KNOWN_CONFIG = "WELL_KNOWN_CONFIG";
    private final Gson preferencesGson = new Gson();
    private final Context context;

    public WellKnownConfigStore(Context context) {
        this.context = context;
    }

    public void set(WellKnownAPI.WellKnownConfig wellKnownConfig) {
        String jsonWellKnownConfig = preferencesGson.toJson(wellKnownConfig);
        context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFERENCE_KEY_WELL_KNOWN_CONFIG, jsonWellKnownConfig)
                .apply();
    }

    public WellKnownAPI.WellKnownConfig get() {
        if (context == null) {
            return null;
        }

        String wellKnownConfigJson = context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getString(PREFERENCE_KEY_WELL_KNOWN_CONFIG, null);

        return preferencesGson.fromJson(
                wellKnownConfigJson,
                WellKnownAPI.WellKnownConfig.class);
    }
}