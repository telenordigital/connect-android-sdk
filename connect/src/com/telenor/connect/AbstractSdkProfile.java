package com.telenor.connect;

import android.content.Context;

import com.google.gson.Gson;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.RestHelper;
import com.telenor.mobileconnect.MobileConnectSdkProfile;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.telenor.connect.utils.ConnectUtils.PREFERENCES_FILE;

public abstract class AbstractSdkProfile implements SdkProfile {

    private ConnectIdService connectIdService;
    private WellKnownAPI.WellKnownConfig wellKnownConfig;

    protected Context context;
    protected boolean confidentialClient;
    private volatile boolean isInitialized = false;
    private LastSeenConfigStore lastSeenStore;

    public AbstractSdkProfile(
            Context context,
            boolean confidentialClient) {
        this.context = context;
        this.confidentialClient = confidentialClient;

        this.lastSeenStore = new LastSeenConfigStore();
        WellKnownAPI.WellKnownConfig lastSeen = lastSeenStore.getWellKnownConfig();
        if (lastSeen != null) {
            wellKnownConfig = lastSeenStore.getWellKnownConfig();
        }
    }

    public abstract String getWellKnownEndpoint();

    @Override
    public Context getContext() {
        return context;
    }

    public WellKnownAPI.WellKnownConfig getWellKnownConfig() {
        return wellKnownConfig;
    }

    @Override
    public boolean isConfidentialClient() {
        return confidentialClient;
    }

    @Override
    public ConnectIdService getConnectIdService() {
        return connectIdService;
    }

    @Override
    public void onFinishAuthorization(boolean success) {
        if (success) {
            lastSeenStore.setWellKnownConfig(wellKnownConfig);
        }
    }

    public void setConnectIdService(ConnectIdService connectIdService) {
        this.connectIdService = connectIdService;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    protected void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    protected void deInitialize() {
        isInitialized = false;
        wellKnownConfig = null;
    }

    protected void initializeAndContinueAuthorizationFlow(final OnStartAuthorizationCallback callback) {
        if (isInitialized) {
            callback.onSuccess();
            return;
        }
        RestHelper.
                getWellKnownApi(getWellKnownEndpoint()).getWellKnownConfig(
                new Callback<WellKnownAPI.WellKnownConfig>() {
                    @Override
                    public void success(WellKnownAPI.WellKnownConfig config, Response response) {
                        wellKnownConfig = config;
                        isInitialized = true;
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        wellKnownConfig = null;
                        isInitialized = true;
                        callback.onSuccess();
                    }
                });
    }

    private class LastSeenConfigStore {

        private static final String PREFERENCE_KEY_WELL_KNOWN_CONFIG = "WELL_KNOWN_CONFIG";
        private final Gson preferencesGson = new Gson();

        private void setWellKnownConfig(WellKnownAPI.WellKnownConfig wellKnownConfig) {
            String jsonWellKnownConfig = preferencesGson.toJson(wellKnownConfig);
            context
                    .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREFERENCE_KEY_WELL_KNOWN_CONFIG, jsonWellKnownConfig)
                    .apply();
        }

        private WellKnownAPI.WellKnownConfig getWellKnownConfig() {
            String wellKnownConfigJson = context
                    .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                    .getString(PREFERENCE_KEY_WELL_KNOWN_CONFIG, null);

            return preferencesGson.fromJson(
                    wellKnownConfigJson,
                    WellKnownAPI.WellKnownConfig.class);
        }
    }
}
