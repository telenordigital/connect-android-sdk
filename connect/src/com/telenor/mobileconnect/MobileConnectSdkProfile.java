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
import com.telenor.connect.utils.RestHelper;
import com.telenor.mobileconnect.id.MobileConnectAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI.OperatorDiscoveryResult;
import static com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI.OperatorSelectionResult;

public class MobileConnectSdkProfile extends AbstractSdkProfile {

    private OperatorDiscoveryConfig operatorDiscoveryConfig;
    private volatile OperatorDiscoveryResult operatorDiscoveryResult;
    private volatile OperatorDiscoveryAPI operatorDiscoveryApi;

    public MobileConnectSdkProfile(
            Context context,
            final OperatorDiscoveryConfig operatorDiscoveryConfig,
            boolean confidentialClient) {
        super(context, confidentialClient);
        this.operatorDiscoveryConfig = operatorDiscoveryConfig;
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
    public synchronized void onStartAuthorization(
            Map<String, String> parameters,
            List<String> uiLocales,
            OnStartAuthorizationCallback callback) {
        fetchStartUri(parameters, uiLocales, null, callback);
    }

    @Override
    public synchronized void initializeFromUri(
            Map<String, String> parameters,
            List<String> uiLocales,
            Uri initFrom,
            final OnDeliverAuthorizationUriCallback callback) {
        fetchStartUri(parameters, uiLocales, initFrom, new OnStartAuthorizationCallback() {
            @Override
            public void onDivert(Uri startUri) {
                throw new RuntimeException("Unexpected callback!");
            }

            @Override
            public void onSuccess(Uri startUri) {
                callback.onSuccess(startUri);
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });
    }

    private void fetchStartUri(
            final Map<String, String> parameters,
            final List<String> uiLocales,
            final Uri initFrom,
            final OnStartAuthorizationCallback callback) {

        if (isInitialized()) {
            callback.onSuccess(getAuthorizationStartUri(parameters, uiLocales));
            return;
        }

        final String mcc;
        final String mnc;

        if (initFrom == null) {
            TelephonyManager phMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = phMgr.getNetworkOperator();

            if (TextUtils.isEmpty(networkOperator)) {
                handleOperatorDiscoveryFailure(callback);
                return;
            }

            mcc = networkOperator.substring(0, 3);
            mnc = networkOperator.substring(3);
        } else {
            String mcc_mnc = initFrom.getQueryParameter("mcc_mnc");
            if (mcc_mnc == null) {
                callback.onError();
                return;
            }
            String[] mccAndMnc = mcc_mnc.split("_");
            if (mccAndMnc.length != 2) {
                callback.onError();
                return;
            }
            mcc = mccAndMnc[0];
            mnc = mccAndMnc[1];

            String subscriber_id = initFrom.getQueryParameter("subscriber_id");
            if (subscriber_id != null) {
                parameters.put(
                        "login_hint",
                        String.format("ENCR_MSISDN:%s", subscriber_id));
            }
        }

        getOperatorDiscoveryApi().getOperatorDiscoveryResult_ForMccMnc(
                getOperatorDiscoveryAuthHeader(),
                operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri(),
                mcc,
                mnc,
                new Callback<OperatorDiscoveryResult>() {
                    @Override
                    public void success(
                            OperatorDiscoveryResult operatorDiscoveryResult,
                            Response response) {
                        initAndContinue(parameters, uiLocales, operatorDiscoveryResult, callback);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (initFrom != null) {
                            callback.onError();
                            return;
                        }
                        handleOperatorDiscoveryFailure(callback);
                    }
                });
    }

    private void handleOperatorDiscoveryFailure(final OnStartAuthorizationCallback callback) {
        getOperatorDiscoveryApi().getOperatorSelectionResult(
                getOperatorDiscoveryAuthHeader(),
                operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri(),
                new Callback<OperatorSelectionResult>() {
                    @Override
                    public void success(
                            OperatorSelectionResult operatorSelectionResult,
                            Response response) {
                        String endpoint = operatorSelectionResult.getEndpoint();
                        if (endpoint == null) {
                            callback.onError();
                            return;
                        }
                        callback.onDivert(Uri.parse(endpoint));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.onError();
                    }
                });
    }

    private OperatorDiscoveryAPI getOperatorDiscoveryApi() {
        return  RestHelper.getOperatorDiscoveryApi(
                    operatorDiscoveryConfig.getOperatorDiscoveryEndpoint());
    }

    private void initAndContinue(
            Map<String, String> parameters,
            List<String> uiLocales,
            OperatorDiscoveryResult odResult,
            OnDeliverAuthorizationUriCallback callback) {
        operatorDiscoveryResult = odResult;
        HttpUrl url = getApiUrl();
        MobileConnectAPI mobileConnectApi =
                RestHelper.getMobileConnectApi(
                        String.format(
                                "%s://%s",
                                url.scheme(),
                                url.host()));
        setConnectIdService(
                new ConnectIdService(
                        new TokenStore(context),
                        new MobileConnectAPIAdapter(mobileConnectApi),
                        getClientId(),
                        getRedirectUri()));
        initializeAndContinueAuthorizationFlow(parameters, uiLocales, callback);
    }

    public void deInitialize() {
        super.deInitialize();
        operatorDiscoveryApi = null;
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
