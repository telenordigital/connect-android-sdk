package com.telenor.mobileconnect;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.SdkProfile;
import com.telenor.connect.id.ConnectAPI;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.id.TokenStore;
import com.telenor.connect.id.UserInfo;
import com.telenor.connect.utils.RestHelper;
import com.telenor.mobileconnect.id.MobileConnectAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;
import com.telenor.mobileconnect.operatordiscovery.WellKnownAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit.Callback;
import retrofit.ResponseCallback;

public class MobileConnectSdkProfile implements SdkProfile {

    private OperatorDiscoveryConfig operatorDiscoveryConfig;
    private OperatorDiscoveryAPI.OperatorDiscoveryResult operatorDiscoveryResult;
    private WellKnownAPI.WellKnownResult wellKnownResult;
    private OperatorDiscoveryAPI operatorDiscoveryApi;
    private ConnectIdService connectIdService;

    private Context context;
    private boolean useStaging;
    private boolean confidentialClient;
    private boolean isInitialized = false;

    public MobileConnectSdkProfile(
            Context context,
            final OperatorDiscoveryConfig operatorDiscoveryConfig,
            boolean useStaging,
            boolean confidentialClient) {
        this.context = context;
        this.operatorDiscoveryConfig = operatorDiscoveryConfig;
        this.useStaging = useStaging;
        this.confidentialClient = confidentialClient;
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
    public boolean isConfidentialClient() {
        return confidentialClient;
    }

    @Override
    public String getRedirectUri() {
        return operatorDiscoveryConfig.getOperatorDiscoveryRedirectUri();
    }

    @Override
    public ConnectIdService getConnectIdService() {
        return connectIdService;
    }

    @Override
    public String getExpectedIssuer(String actualIssuer) {
        // discrepancy between .well-known configuration and the actual issuer returned
        if (operatorDiscoveryResult.getBasePath().contains("telenordigital.com")) {
            return actualIssuer;
        }
        return wellKnownResult.getIssuer();
    }

    @Override
    public List<String> getExpectedAudiences(List<String> actualAudiences) {
        return new ArrayList<String>(actualAudiences);
    }

    @Override
    public DoNext onAuthorize(Map<String, String> parameters) {
        OperatorDiscoveryAPI.OperatorDiscoveryResult odResult = null;
        final String msisdn = readPhoneNumber();
        if (msisdn != null) {
            odResult = getOperatorDiscoveryResult(msisdn);
        }
        if (odResult != null) {
            parameters.put("login_hint", String.format("ENCR_MSISDN:%s", odResult.getSubscriberId()));
            if (isInitialized) {
                return DoNext.proceed;
            } else {
                if (initialize(odResult)) {
                    return DoNext.proceed;
                }
            }
        } else {
            if (isInitialized) {
                return DoNext.proceed;
            } else {
                odResult = getOperatorDiscoveryResult();
                if (odResult != null) {
                    if (initialize(odResult)) {
                        return DoNext.proceed;
                    }
                }
            }
        }
        return DoNext.cancel;
    }

    private String readPhoneNumber() {
        TelephonyManager phMgr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        try {
            String msisdn = phMgr.getLine1Number();
            return (TextUtils.isEmpty(msisdn) ? null : msisdn);
        } catch (RuntimeException e) {
            ;
        }
        return null;
    }

    private OperatorDiscoveryAPI.OperatorDiscoveryResult getOperatorDiscoveryResult() {
        TelephonyManager phMgr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String networkOperator = phMgr.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            final String mcc = "242"; //networkOperator.substring(0, 3);
            final String mnc = "01"; // networkOperator.substring(3);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<OperatorDiscoveryAPI.OperatorDiscoveryResult> operatorDiscoveryResultFuture =
                    executor.submit(new Callable<OperatorDiscoveryAPI.OperatorDiscoveryResult>() {
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
            } catch (InterruptedException | ExecutionException | RuntimeException e) {
                ;
            }
        }
        return null;
    }

    private OperatorDiscoveryAPI.OperatorDiscoveryResult getOperatorDiscoveryResult(final String msisdn) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<OperatorDiscoveryAPI.OperatorDiscoveryResult> operatorDiscoveryResultFuture =
                executor.submit(new Callable<OperatorDiscoveryAPI.OperatorDiscoveryResult>() {
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
        } catch (InterruptedException | ExecutionException | RuntimeException e) {
            ;
        }
        return null;
    }

    private WellKnownAPI.WellKnownResult getWellKnownConfig() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<WellKnownAPI.WellKnownResult> wellKnownResultFuture =
                executor.submit(new Callable<WellKnownAPI.WellKnownResult>() {
                    @Override
                    public WellKnownAPI.WellKnownResult call() throws Exception {
                        return RestHelper.getMobileConnectWellKnownApi(
                                operatorDiscoveryResult.getBasePath()).getWellKnownConfig();
                    }
                });
        try {
            return wellKnownResultFuture.get();
        } catch (InterruptedException | ExecutionException | RuntimeException e) {
            ;
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
            operatorDiscoveryResult = odResult;
            HttpUrl url = getApiUrl();
            MobileConnectAPI mobileConnectApi =
                    RestHelper.getMobileConnectApi(
                            String.format(
                                    "%s://%s",
                                    url.scheme(),
                                    url.host()));
            connectIdService =
                    new ConnectIdService(
                            new TokenStore(context),
                            new MobileConnectAPIAdapter(mobileConnectApi),
                            getClientId(),
                            getRedirectUri());
            wellKnownResult = getWellKnownConfig();
            isInitialized = (wellKnownResult != null);
        } catch (RuntimeException e) {
            ;
        }
        return isInitialized;
    }

    public void deInitialize() {
        operatorDiscoveryApi = null;
        operatorDiscoveryResult = null;
        connectIdService = null;
        wellKnownResult = null;
        isInitialized = false;
    }

    public String getAuthorizationHeader() {
        return "Basic " + Base64.encodeToString(
                String.format("%s:%s",
                        getClientId(),
                        getClientSecret()).getBytes(),
                Base64.NO_WRAP);
    }

    public String getOperatorPrefix() {
        return operatorDiscoveryResult.getServingOperator();
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

        public MobileConnectAPIAdapter(MobileConnectAPI mobileConnectApi) {
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
                    getOperatorPrefix(),
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
                    getOperatorPrefix(),
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
                    getOperatorPrefix(),
                    token,
                    callback);
        }

        @Override
        public void getUserInfo(
                String auth,
                Callback<UserInfo> userInfoCallback) {
            mobileConnectApi.getUserInfo(
                    auth,
                    getOperatorPrefix(),
                    userInfoCallback);
        }
    }
}
