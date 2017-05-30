package com.telenor.connect;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.RestHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class AbstractSdkProfile implements SdkProfile {

    private volatile ConnectIdService connectIdService;
    private volatile WellKnownAPI.WellKnownConfig wellKnownConfig;

    protected Context context;
    protected boolean confidentialClient;
    private volatile boolean isInitialized = false;

    public AbstractSdkProfile(
            Context context,
            boolean confidentialClient) {
        this.context = context;
        this.confidentialClient = confidentialClient;
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

    protected void initializeAndContinueAuthorizationFlow(
            final Map<String, String> parameters,
            final List<String> uiLocales,
            final OnDeliverAuthorizationUriCallback callback) {
        if (isInitialized) {
            callback.onSuccess(getAuthorizationStartUri(parameters, uiLocales));
            return;
        }
        RestHelper.
                getWellKnownApi(getWellKnownEndpoint()).getWellKnownConfig(
                new Callback<WellKnownAPI.WellKnownConfig>() {
                    @Override
                    public void success(WellKnownAPI.WellKnownConfig config, Response response) {
                        wellKnownConfig = config;
                        isInitialized = true;
                        callback.onSuccess(getAuthorizationStartUri(parameters, uiLocales));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        wellKnownConfig = null;
                        isInitialized = true;
                        callback.onSuccess(getAuthorizationStartUri(parameters, uiLocales));
                    }
                });
    }

    protected synchronized Uri getAuthorizationStartUri(
            Map<String, String> parameters,
            List<String> uiLocales) {
        if (getClientId() == null) {
            throw new ConnectException("Client ID not specified in application manifest.");
        }
        if (getRedirectUri() == null) {
            throw new ConnectException("Redirect URI not specified in application manifest.");
        }

        if (parameters.get("scope") == null || parameters.get("scope").isEmpty()) {
            throw new IllegalStateException("Cannot log in without scope tokens.");
        }

        if (TextUtils.isEmpty(parameters.get("state"))) {
            parameters.put("state", UUID.randomUUID().toString());
        }

        return getAuthorizeUri(parameters, uiLocales);
    }
}
