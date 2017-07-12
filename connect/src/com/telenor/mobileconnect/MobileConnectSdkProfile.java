package com.telenor.mobileconnect;

import android.content.Context;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.AbstractSdkProfile;
import com.telenor.connect.id.ConnectAPI;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.id.TokenStore;
import com.telenor.connect.id.UserInfo;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.RestHelper;
import com.telenor.connect.utils.Validator;
import com.telenor.mobileconnect.id.MobileConnectAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MobileConnectSdkProfile extends AbstractSdkProfile {

    private OperatorDiscoveryConfig operatorDiscoveryConfig;
    private volatile OperatorDiscoveryAPI.OperatorDiscoveryResult operatorDiscoveryResult;
    private final OperatorDiscoveryConfigStore lastSeenStore;

    private String lastAuthNonce;

    public MobileConnectSdkProfile(
            Context context,
            final OperatorDiscoveryConfig operatorDiscoveryConfig,
            boolean confidentialClient) {
        super(context, confidentialClient);
        this.operatorDiscoveryConfig = operatorDiscoveryConfig;

        lastSeenStore = new OperatorDiscoveryConfigStore(context);
        OperatorDiscoveryAPI.OperatorDiscoveryResult lastSeen = lastSeenStore.get();
        if (lastSeen != null) {
            operatorDiscoveryResult = lastSeen;
            setConnectIdService(createConnectIdService());
        }
    }

    @Override
    public HttpUrl getApiUrl() {
        String host = operatorDiscoveryResult.getMobileConnectApiUrl().host();
        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder
                .scheme(operatorDiscoveryResult.getMobileConnectApiUrl().scheme())
                .host(host);
        for (String seg : operatorDiscoveryResult.getMobileConnectApiUrl().pathSegments()) {
            builder.addPathSegment(seg);
        }
        return builder.build();
    }

    @Override
    public String getClientId() {
        return operatorDiscoveryResult.getClientId();
    }

    @Override
    public String getClientSecret() {
        return operatorDiscoveryResult.getClientSecret();
    }

    @Override
    public String getRedirectUri() {
        return operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri();
    }

    @Override
    public String getExpectedIssuer() {
        if (getWellKnownConfig() != null) {
            return getWellKnownConfig().getIssuer();
        }
        return operatorDiscoveryResult.getBasePath();
    }

    @Override
    public List<String> getExpectedAudiences() {
        return new ArrayList<>(Arrays.asList(getClientId()));
    }

    @Override
    public Uri getAuthorizeUri(Map<String, String> parameters, List<String> locales) {
        Uri.Builder builder = ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                getClientId(),
                getRedirectUri(),
                locales,
                getApiUrl())
                .buildUpon();
        for (String pathSeg : getApiUrl().pathSegments()) {
            builder.appendPath(pathSeg);
        }
        return builder.build();
    }

    @Override
    public void onStartAuthorization(Map<String, String> parameters, final OnStartAuthorizationCallback callback) {
        super.onStartAuthorization(parameters, callback);
        lastAuthNonce = UUID.randomUUID().toString();
        parameters.put("nonce", lastAuthNonce);
        if (parameters.get(ConnectUtils.ACR_VALUES_PARAM_NAME) == null) {
            parameters.put(ConnectUtils.ACR_VALUES_PARAM_NAME, "2");
        }

        if (isInitialized()) {
            callback.onSuccess();
            return;
        }

        TelephonyManager phMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = phMgr.getNetworkOperator();

        if (TextUtils.isEmpty(networkOperator)) {
            callback.onError();
            return;
        }

        final String mcc = networkOperator.substring(0, 3);
        final String mnc = networkOperator.substring(3);
        getOperatorDiscoveryApi().getOperatorDiscoveryResult_ForMccMnc(
                getOperatorDiscoveryAuthHeader(),
                operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri(),
                mcc,
                mnc,
                new Callback<OperatorDiscoveryAPI.OperatorDiscoveryResult>() {
                    @Override
                    public void success(OperatorDiscoveryAPI.OperatorDiscoveryResult operatorDiscoveryResult, Response response) {
                        initAndContinue(operatorDiscoveryResult, callback);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.onError();
                    }
                });
    }

    @Override
    public void onFinishAuthorization(boolean success) {
        super.onFinishAuthorization(success);
        if (success) {
            lastSeenStore.set(operatorDiscoveryResult);
        }
    }

    @Override
    public void validateTokens(ConnectTokensTO tokens, Date serverTimestamp) {
        super.validateTokens(tokens, serverTimestamp);
        Validator.notNull(tokens.getIdToken(), "id_token");
        Validator.notNullOrEmpty(tokens.getIdToken().getNonce(), "nonce");
        Validator.notDifferent(lastAuthNonce, tokens.getIdToken().getNonce(), "nonce");
    }

    private OperatorDiscoveryAPI getOperatorDiscoveryApi() {
        return  RestHelper.getOperatorDiscoveryApi(
                    operatorDiscoveryConfig.getOperatorDiscoveryEndpoint());
    }

    private void initAndContinue(
            OperatorDiscoveryAPI.OperatorDiscoveryResult odResult,
            OnStartAuthorizationCallback callback) {
        operatorDiscoveryResult = odResult;
        setConnectIdService(createConnectIdService());
        initializeAndContinueAuthorizationFlow(callback);
    }

    private ConnectIdService createConnectIdService() {
        HttpUrl url = getApiUrl();
        MobileConnectAPI mobileConnectApi =
                RestHelper.getMobileConnectApi(
                        String.format(
                                "%s://%s",
                                url.scheme(),
                                url.host()));
        return new ConnectIdService(
                new TokenStore(context),
                new MobileConnectAPIAdapter(mobileConnectApi),
                getClientId(),
                getRedirectUri());
    }

    public void deInitialize() {
        super.deInitialize();
        setInitialized(false);
    }

    @Override
    public String getWellKnownEndpoint() {
        return operatorDiscoveryResult.getWellKnownEndpoint();
    }

    private String getAuthorizationHeader() {
        return "Basic " + Base64.encodeToString(
                String.format("%s:%s",
                        getClientId(),
                        getClientSecret()).getBytes(),
                Base64.NO_WRAP);
    }

    private String getOperatorDiscoveryAuthHeader() {
        return Base64.encodeToString(
                String.format("%s:%s",
                        operatorDiscoveryConfig.getOperatorDiscoveryClientId(),
                        operatorDiscoveryConfig.getOperatorDiscoveryClientSecret()).getBytes(),
                Base64.NO_WRAP);
    }

    private class MobileConnectAPIAdapter implements ConnectAPI {

        private MobileConnectAPI mobileConnectApi;

        MobileConnectAPIAdapter(MobileConnectAPI mobileConnectApi) {
            this.mobileConnectApi = mobileConnectApi;
        }

        @Override
        public void getAccessTokens(
                String grantType,
                String code,
                String redirectUri,
                String clientId,
                Callback<ConnectTokensTO> tokens) {
            mobileConnectApi.getAccessTokens(
                    getAuthorizationHeader(),
                    operatorDiscoveryResult.getPath("token"),
                    grantType,
                    code,
                    redirectUri,
                    tokens);
        }

        @Override
        public void refreshAccessTokens(
                String grantType,
                String refreshToken,
                String clientId,
                Callback<ConnectTokensTO> tokens) {
            mobileConnectApi.refreshAccessTokens(
                    getAuthorizationHeader(),
                    operatorDiscoveryResult.getPath("token"),
                    grantType,
                    refreshToken,
                    tokens);
        }

        @Override
        public void revokeToken(
                String clientId,
                String token,
                ResponseCallback callback) {
            mobileConnectApi.revokeToken(
                    getAuthorizationHeader(),
                    operatorDiscoveryResult.getPath("tokenrevoke"),
                    token,
                    callback);
        }

        @Override
        public void getUserInfo(
                String auth,
                Callback<UserInfo> userInfoCallback) {
            mobileConnectApi.getUserInfo(
                    auth,
                    operatorDiscoveryResult.getPath("userinfo"),
                    userInfoCallback);
        }
    }
}
