package com.telenor.connect;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.IdTokenValidator;
import com.telenor.connect.utils.RestHelper;
import com.telenor.connect.utils.Validator;

import java.util.Collection;
import java.util.Date;
import java.util.List;
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

    private String lastAuthState;

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
    public void initializeAuthorizationFlow(
            Activity activity,
            final AuthFlowInitializationCallback callback) {

        final ParametersHolder parameters = getLoginParams(activity);
        final List<String> uiLocales = getUiLocales(activity);

        if (isInitialized) {
            callback.onSuccess(getAuthorizeUri(parameters, uiLocales), wellKnownConfig);
            return;
        }

        initialize(new InitializationCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess(getAuthorizeUri(parameters, uiLocales), getWellKnownConfig());
            }

            @Override
            public void onError(ConnectInitializationError error) {
                callback.onSuccess(getAuthorizeUri(parameters, uiLocales), null);
            }
        });
    }

    protected void initialize(final InitializationCallback callback) {
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

    public interface InitializationCallback {
        void onSuccess();
        void onError(ConnectInitializationError error);
    }

    protected static ParametersHolder getLoginParams(Activity activity) {
        @SuppressWarnings("unchecked")
        ParametersHolder loginParams =  (ParametersHolder) activity
                .getIntent()
                .getExtras()
                .get(ConnectUtils.LOGIN_PARAMS);
        return loginParams;
    }

    protected static List<String> getUiLocales(Activity activity) {
        @SuppressWarnings("unchecked")
        final List<String> uiLocales = (List<String>) activity
                .getIntent()
                .getExtras()
                .get(ConnectUtils.UI_LOCALES);
        return uiLocales;
    }

    protected void previewParameters(ParametersHolder parameters) {
        if (parameters.get("state") == null) {
            parameters.add("state", UUID.randomUUID().toString());
        }
        lastAuthState = parameters.get("state").get(0);

        String mccMnc = ConnectSdk.getMccMnc();
        if (!TextUtils.isEmpty(mccMnc) && wellKnownConfig != null &&
                !(isEmpty(wellKnownConfig.getNetworkAuthenticationTargetIps())
                        && isEmpty(wellKnownConfig.getNetworkAuthenticationTargetUrls()))) {
                    parameters.add("login_hint", String.format("MCCMNC:%s", mccMnc));
        }

        if (!ConnectSdk.isCellularDataNetworkConnected()) {
            parameters.add("prompt", "no_seam");
        }
    }

    @Override
    public String getLastAuthState() {
        return lastAuthState;
    }

    @Override
    public void validateTokens(ConnectTokensTO tokens, Date serverTimestamp) {
        Validator.notNullOrEmpty(tokens.getAccessToken(), "access_token");
        Validator.notNullOrEmpty(tokens.getTokenType(), "token_type");

        if (tokens.getIdToken() != null) {
            IdTokenValidator.validate(tokens.getIdToken(), serverTimestamp);
        }
    }

    private static boolean isEmpty(Collection c) {
        if (c == null) {
            return true;
        }
        return c.isEmpty();
    }
}
