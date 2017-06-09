package com.telenor.mobileconnect;

import android.content.Context;

import com.google.gson.Gson;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;

import static com.telenor.connect.id.TokenStore.PREFERENCES_FILE;

public class OperatorDiscoveryConfigStore {

    private static final String PREFERENCE_KEY_OD_RESULT = "OD_RESULT";
    private final Gson preferencesGson = new Gson();
    private final Context context;

    public OperatorDiscoveryConfigStore(Context context) {
        this.context = context;
    }

    public void set(
            OperatorDiscoveryAPI.OperatorDiscoveryResult odResult) {
        String jsonOdResult = preferencesGson.toJson(odResult);
        context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFERENCE_KEY_OD_RESULT, jsonOdResult)
                .apply();
    }

    public OperatorDiscoveryAPI.OperatorDiscoveryResult get() {
        String wellKnownConfigJson = context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getString(PREFERENCE_KEY_OD_RESULT, null);
        return preferencesGson.fromJson(
                wellKnownConfigJson,
                OperatorDiscoveryAPI.OperatorDiscoveryResult.class);
    }
}
