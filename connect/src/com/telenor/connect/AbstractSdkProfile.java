package com.telenor.connect;

import android.content.Context;
import android.text.TextUtils;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.utils.IdTokenValidator;
import com.telenor.connect.utils.RestHelper;
import com.telenor.connect.utils.Validator;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class AbstractSdkProfile implements SdkProfile {
    private ConnectIdService connectIdService;
    private WellKnownAPI.WellKnownConfig wellKnownConfig;

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
    public void onStartAuthorization(
            Map<String, String> parameters,
            OnStartAuthorizationCallback callback) {
        if (TextUtils.isEmpty(parameters.get("state"))) {
            parameters.put("state", UUID.randomUUID().toString());
        }
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

    @Override
    public void validateTokens(ConnectTokensTO tokens, Date serverTimestamp) {
        Validator.notNullOrEmpty(tokens.getAccessToken(), "access_token");
        Validator.notNullOrEmpty(tokens.getTokenType(), "token_type");

        if (tokens.getIdToken() != null) {
            IdTokenValidator.validate(tokens.getIdToken(), serverTimestamp);
        }
    }
}
