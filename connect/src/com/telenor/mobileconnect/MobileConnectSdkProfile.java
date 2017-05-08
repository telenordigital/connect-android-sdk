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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import retrofit.Callback;
import retrofit.ResponseCallback;

public class MobileConnectSdkProfile extends AbstractSdkProfile {

    private OperatorDiscoveryConfig operatorDiscoveryConfig;
    private OperatorDiscoveryAPI.OperatorDiscoveryResult operatorDiscoveryResult;
    private OperatorDiscoveryAPI operatorDiscoveryApi;
    private boolean isInitialized = false;

    public MobileConnectSdkProfile(
            Context context,
            final OperatorDiscoveryConfig operatorDiscoveryConfig,
            boolean useStaging,
            boolean confidentialClient) {
        super(context, useStaging, confidentialClient);
        this.operatorDiscoveryConfig = operatorDiscoveryConfig;
    }

    @Override
    public HttpUrl getApiUrl() {
        String host = operatorDiscoveryResult.getMobileConnectApiUrl().host();
        if (useStaging) {
            // will have no effect on non-Telenor subscribers
            host = host.replace(
                    "connect.telenordigital.com",
                    "connect.staging.telenordigital.com");
        }
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
    public String getExpectedIssuer(String actualIssuer) {
        // discrepancy between .well-known configuration and the actual issuer returned
        if (operatorDiscoveryResult.getBasePath().contains("telenordigital.com")) {
            return actualIssuer;
        }
        if (getWellKnownConfig() != null) {
            return getWellKnownConfig().getIssuer();
        }
        return actualIssuer;
    }

    @Override
    public List<String> getExpectedAudiences(List<String> actualAudiences) {
        return new ArrayList<>(actualAudiences);
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
    public DoNext onStartAuthorization(Map<String, String> parameters) {
        OperatorDiscoveryAPI.OperatorDiscoveryResult odResult = null;

        final String msisdn = readPhoneNumber();
        if (msisdn != null) {
            odResult = getOperatorDiscoveryResult(msisdn);
        }

        if (odResult != null) {
            parameters.put("login_hint", String.format("ENCR_MSISDN:%s", odResult.getSubscriberId()));
        }

        if (isInitialized || initialize(odResult)) {
            return DoNext.proceed;
        }

        return DoNext.cancel;
    }

    private String readPhoneNumber() {
        TelephonyManager phMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            String msisdn = phMgr.getLine1Number();
            return (TextUtils.isEmpty(msisdn) ? null : msisdn);
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    private OperatorDiscoveryAPI.OperatorDiscoveryResult getOperatorDiscoveryResult() {
        TelephonyManager phMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = phMgr.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            final String mcc = networkOperator.substring(0, 3);
            final String mnc = networkOperator.substring(3);
            Future<OperatorDiscoveryAPI.OperatorDiscoveryResult> operatorDiscoveryResultFuture =
                    sExecutor.submit(new Callable<OperatorDiscoveryAPI.OperatorDiscoveryResult>() {
                        @Override
                        public OperatorDiscoveryAPI.OperatorDiscoveryResult call() throws Exception {
                            return getOperatorDiscoveryApi().getOperatorDiscoveryResult_ForMccMnc(
                                    getOperatorDiscoveryAuthHeader(),
                                    operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri(),
                                    mcc,
                                    mnc
                            );
                        }
                    });
            try {
                return operatorDiscoveryResultFuture.get();
            } catch (InterruptedException | ExecutionException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private OperatorDiscoveryAPI.OperatorDiscoveryResult getOperatorDiscoveryResult(final String msisdn) {
        Future<OperatorDiscoveryAPI.OperatorDiscoveryResult> operatorDiscoveryResultFuture =
                sExecutor.submit(new Callable<OperatorDiscoveryAPI.OperatorDiscoveryResult>() {
                    @Override
                    public OperatorDiscoveryAPI.OperatorDiscoveryResult call() throws Exception {
                        return getOperatorDiscoveryApi().getOperatorDiscoveryResult_ForMsisdn(
                                getOperatorDiscoveryAuthHeader(),
                                new OperatorDiscoveryAPI.BodyForMsisdn(
                                        operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri(),
                                        msisdn)
                        );
                    }
                });
        try {
            return operatorDiscoveryResultFuture.get();
        } catch (InterruptedException | ExecutionException | RuntimeException ignored) {
        }
        return null;
    }

    private OperatorDiscoveryAPI getOperatorDiscoveryApi() {
        if (operatorDiscoveryApi == null) {
            operatorDiscoveryApi = RestHelper.getOperatorDiscoveryAPI(
                    operatorDiscoveryConfig.getOperatorDiscoveryEndpoint());
        }
        return operatorDiscoveryApi;
    }

    private boolean initialize(OperatorDiscoveryAPI.OperatorDiscoveryResult odResult) {
        isInitialized = false;
        try {
            if (odResult == null) {
                odResult = getOperatorDiscoveryResult();
            }
            if (odResult == null) {
                return false;
            }
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
            isInitialized = true;
            return true;
        } catch (RuntimeException ignored) {
        }
        return false;
    }

    public void deInitialize() {
        deInitialize();
        setConnectIdService(null);
        operatorDiscoveryApi = null;
        operatorDiscoveryResult = null;
        isInitialized = false;
    }

    @Override
    protected String getWellKnownEndpoint() {
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
                String grant_type,
                String code,
                String redirect_uri,
                String client_id,
                Callback<ConnectTokensTO> tokens) {
            mobileConnectApi.getAccessTokens(
                    getAuthorizationHeader(),
                    operatorDiscoveryResult.getPath("token"),
                    grant_type,
                    code,
                    redirect_uri,
                    tokens);
        }

        @Override
        public void refreshAccessTokens(
                String grant_type,
                String refresh_token,
                String client_id,
                Callback<ConnectTokensTO> tokens) {
            mobileConnectApi.refreshAccessTokens(
                    getAuthorizationHeader(),
                    operatorDiscoveryResult.getPath("token"),
                    grant_type,
                    refresh_token,
                    tokens);
        }

        @Override
        public void revokeToken(
                String client_id,
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