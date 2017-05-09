package com.telenor.connect;

import android.content.Context;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.RestHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class AbstractSdkProfile implements SdkProfile {

    private volatile ConnectIdService connectIdService;
    private volatile WellKnownAPI.WellKnownConfig wellKnownConfig;

    protected Context context;
    protected boolean useStaging;
    protected boolean confidentialClient;
    private volatile boolean isInitialized = false;

    public AbstractSdkProfile(
            Context context,
            boolean useStaging,
            boolean confidentialClient) {
        this.context = context;
        this.useStaging = useStaging;
        this.confidentialClient = confidentialClient;
    }

    protected abstract String getWellKnownEndpoint();

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

    public void setConnectIdService(ConnectIdService connectIdService) {
        this.connectIdService = connectIdService;
    }

    protected boolean isInitialized() {
        return isInitialized;
    }

    protected void  setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    protected void deInitialize() {
        isInitialized = false;
        wellKnownConfig = null;
    }

    protected boolean initialize() {
        if (!isInitialized) {
            fetchWellKnownConfig();
            isInitialized = true;
        }
        return isInitialized;
    }

    private void fetchWellKnownConfig() {
        RestHelper.
                getWellKnownApi(getWellKnownEndpoint()).getWellKnownConfig(
                new Callback<WellKnownAPI.WellKnownConfig>() {
                    @Override
                    public void success(WellKnownAPI.WellKnownConfig wellKnownConfig, Response response) {
                        AbstractSdkProfile.this.wellKnownConfig = wellKnownConfig;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        wellKnownConfig = null;
                    }
                });
    }

}
