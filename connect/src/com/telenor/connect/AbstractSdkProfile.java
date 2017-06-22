package com.telenor.connect;

import android.content.Context;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.RestHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class AbstractSdkProfile implements SdkProfile {

    private ConnectIdService connectIdService;
    private volatile WellKnownAPI.WellKnownConfig wellKnownConfig;
    private final Map<String, String> profileStorage = new ConcurrentHashMap<>();

    protected Context context;
    protected boolean confidentialClient;
    private volatile boolean isInitialized = false;
    private final WellKnownConfigStore lastSeenStore;

    public AbstractSdkProfile(
            Context context,
            boolean confidentialClient) {
        this.context = context;
        this.confidentialClient = confidentialClient;
        lastSeenStore = new WellKnownConfigStore(context);
        wellKnownConfig = lastSeenStore.get();
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
            lastSeenStore.set(wellKnownConfig);
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

    @Override
    public void fetchWellknownConfig(final SdkCallback callback) {
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

    @Override
    public void setValue(final String key, final String value) {
        if (value != null) {
            profileStorage.put(key, value);
        }
    }

    @Override
    public String getValue(final String key) {
        return profileStorage.get(key);
    }
}
