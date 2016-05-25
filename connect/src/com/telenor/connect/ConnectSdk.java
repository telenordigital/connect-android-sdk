package com.telenor.connect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.TokenStore;
import com.telenor.connect.id.UserInfo;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.RestHelper;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit.Callback;

public final class ConnectSdk {
    private static String sClientId;
    private static boolean sConfidentialClient = false;
    private static Context sContext;
    private static String sLastAuthState;
    private static ArrayList<Locale> sLocales;
    private static String sPaymentCancelUri;
    private static String sPaymentSuccessUri;
    private static String sRedirectUri;
    private static boolean sSdkInitialized = false;
    private static boolean sUseStaging = false;
    private static ConnectIdService sConnectIdService;

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CLIENT_ID_PROPERTY = "com.telenor.connect.CLIENT_ID";

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CONFIDENTIAL_CLIENT_PROPERTY = "com.telenor.connect.CONFIDENTIAL_CLIENT";

    /**
     * The key to for the payment cancel URI in the Android manifest.
     */
    public static final String PAYMENT_CANCEL_URI_PROPERTY = "com.telenor.connect.PAYMENT_CANCEL_URI";

    /**
     * The key to for the payment success URI in the Android manifest.
     */
    public static final String PAYMENT_SUCCESS_URI_PROPERTY = "com.telenor.connect.PAYMENT_SUCCESS_URI";

    /**
     * The key for the redirect URI in the Android manifest.
     */
    public static final String REDIRECT_URI_PROPERTY = "com.telenor.connect.REDIRECT_URI";

    /**
     * The key to enable the staging environment in the Android manifest.
     */
    public static final String USE_STAGING_PROPERTY = "com.telenor.connect.USE_STAGING";

    public static final String ACTION_LOGIN_STATE_CHANGED =
            "com.telenor.connect.ACTION_LOGIN_STATE_CHANGED";

    public static final String EXTRA_PAYMENT_LOCATION =
            "com.telenor.connect.EXTRA_PAYMENT_LOCATION";

    public static final String EXTRA_CONNECT_TOKENS =
            "com.telenor.connect.EXTRA_CONNECT_TOKENS";

    public static synchronized void authenticate(
            Activity activity,
            int requestCode,
            String... scopeTokens) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("scope", TextUtils.join(" ", scopeTokens));
        authenticate(activity, parameters, requestCode);
    }

    public static synchronized void authenticate(
            Activity activity,
            Map<String, String> parameters,
            int requestCode) {

        Intent intent = getAuthIntent(parameters);
        activity.startActivityForResult(intent, requestCode);
    }

    private static Intent getAuthIntent(Map<String, String> parameters) {
        Intent intent = new Intent();
        intent.setClass(getContext(), ConnectActivity.class);
        intent.setAction(ConnectUtils.LOGIN_ACTION);
        intent.putExtra(ConnectUtils.LOGIN_AUTH_URI,
                getAuthorizeUriAndSetLastAuthState(parameters).toString());
        return intent;
    }

    public static synchronized void authenticate(Activity activity,
            Map<String, String> parameters,
            int customLoadingLayout,
            int requestCode) {
        Intent intent = getAuthIntent(parameters);
        intent.putExtra(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA, customLoadingLayout);
        activity.startActivityForResult(intent, requestCode);
    }

    public static synchronized String getAccessToken() {
        Validator.sdkInitialized();
        return sConnectIdService.getAccessToken();
    }

    public static synchronized void getAccessTokenFromCode(String code, ConnectCallback callback) {
        Validator.sdkInitialized();
        sConnectIdService.getAccessTokenFromCode(code, callback);
    }

    public static synchronized Uri getAuthorizeUriAndSetLastAuthState(
            Map<String, String> parameters) {
        if (ConnectSdk.getClientId() == null) {
            throw new ConnectException("Client ID not specified in application manifest.");
        }
        if (ConnectSdk.getRedirectUri() == null) {
            throw new ConnectException("Redirect URI not specified in application manifest.");
        }

        if (parameters.get("scope") == null || parameters.get("scope").isEmpty()) {
            throw new IllegalStateException("Cannot log in without scope tokens.");
        }

        if (parameters.get("state") == null || parameters.get("state").isEmpty()) {
            parameters.put("state", UUID.randomUUID().toString());
        }
        sLastAuthState = parameters.get("state");

        return ConnectUrlHelper.getAuthorizeUri(
                parameters,
                getClientId(),
                getRedirectUri(),
                getUiLocales(),
                getConnectApiUrl());
    }

    public static HttpUrl getConnectApiUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme("https");
        builder.host(sUseStaging
                ? "connect.staging.telenordigital.com"
                : "connect.telenordigital.com");
        return builder.build();
    }

    public static Context getContext() {
        Validator.sdkInitialized();
        return sContext;
    }

    public static String getClientId() {
        Validator.sdkInitialized();
        return sClientId;
    }

    public static String getLastAuthenticationState() {
        Validator.sdkInitialized();
        return sLastAuthState;
    }

    public static ArrayList<Locale> getLocales() {
        Validator.sdkInitialized();
        return sLocales;
    }

    public static String getPaymentCancelUri() {
        Validator.sdkInitialized();
        return sPaymentCancelUri;
    }

    public static String getPaymentSuccessUri() {
        Validator.sdkInitialized();
        return sPaymentSuccessUri;
    }

    public static String getRedirectUri() {
        Validator.sdkInitialized();
        return sRedirectUri;
    }

    private static ArrayList<String> getUiLocales() {
        ArrayList<String> locales = new ArrayList<>();
        if (ConnectSdk.getLocales() != null && !ConnectSdk.getLocales().isEmpty()) {
            for (Locale locale : ConnectSdk.getLocales()) {
                locales.add(locale.toString());
                locales.add(locale.getLanguage());
            }
        }
        locales.add(Locale.getDefault().toString());
        locales.add(Locale.getDefault().getLanguage());
        return locales;
    }

    public static void initializePayment(Context context, String transactionLocation) {
        Validator.sdkInitialized();
        if (ConnectSdk.getPaymentSuccessUri() == null
                || ConnectSdk.getPaymentCancelUri() == null ) {
            throw new ConnectException("Payment success or cancel URI not specified in application"
                    + "manifest.");
        }

        Intent intent = new Intent();
        intent.setClass(ConnectSdk.getContext(), ConnectActivity.class);
        intent.putExtra(ConnectSdk.EXTRA_PAYMENT_LOCATION, transactionLocation);
        intent.setAction(ConnectUtils.PAYMENT_ACTION);

        Activity activity = (Activity) context;
        activity.startActivityForResult(intent, 1);
    }

    public static synchronized boolean isConfidentialClient() {
        return sConfidentialClient;
    }

    public static synchronized boolean isInitialized() {
        return sSdkInitialized;
    }

    public static void logout() {
        Validator.sdkInitialized();
        sConnectIdService.revokeTokens(sContext);
    }

    public static synchronized void sdkInitialize(Context context) {
        if (sSdkInitialized) {
            return;
        }

        Validator.notNull(context, "context");
        sContext = context;
        loadConnectConfig(ConnectSdk.sContext);

        sSdkInitialized = true;
        sConnectIdService = new ConnectIdService(
                new TokenStore(context),
                RestHelper.getConnectApi(getConnectApiUrl().toString()),
                sClientId,
                sRedirectUri);
    }

    public static void setLocales(Locale... locales) {
        sLocales = new ArrayList<Locale>(Arrays.asList(locales));
    }

    public static void setLocales(ArrayList<Locale> locales) {
        sLocales = locales;
    }

    public static void updateTokens(ConnectCallback callback) {
        Validator.sdkInitialized();
        sConnectIdService.updateTokens(callback);
    }

    private static void loadConnectConfig(Context context) {
        if (context == null) {
            return;
        }

        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        if (ai == null || ai.metaData == null) {
            throw new ConnectException("No application metadata was found.");
        }

        Object clientIdObject = ai.metaData.get(CLIENT_ID_PROPERTY);
        if (clientIdObject instanceof String) {
            String clientIdString = (String) clientIdObject;
            sClientId = clientIdString;
        }

        Object confidentialClientObject = ai.metaData.get(CONFIDENTIAL_CLIENT_PROPERTY);
        if (confidentialClientObject instanceof Boolean) {
            sConfidentialClient = (Boolean) confidentialClientObject;
        }

        Object redirectUriObject = ai.metaData.get(REDIRECT_URI_PROPERTY);
        if (redirectUriObject instanceof String) {
            String redirectUriString = (String) redirectUriObject;
            sRedirectUri = redirectUriString;
        }

        Object useStagingObject = ai.metaData.get(USE_STAGING_PROPERTY);
        if (useStagingObject instanceof Boolean) {
            sUseStaging = (Boolean) useStagingObject;
        }

        Object paymentCancelUriObject = ai.metaData.get(PAYMENT_CANCEL_URI_PROPERTY);
        if (paymentCancelUriObject instanceof String) {
            String paymentCancelUriString = (String) paymentCancelUriObject;
            sPaymentCancelUri = paymentCancelUriString;
        }

        Object paymentSuccessUriObject = ai.metaData.get(PAYMENT_SUCCESS_URI_PROPERTY);
        if (paymentSuccessUriObject instanceof String) {
            String paymentSuccessUriString = (String) paymentSuccessUriObject;
            sPaymentSuccessUri = paymentSuccessUriString;
        }
    }

    /**
     * @return the subject's ID (sub), if one is signed in. Otherwise {@code null}.
     */
    public static String getSubjectId() {
        Validator.sdkInitialized();
        return sConnectIdService.getSubjectId();
    }

    /**
     * Fetches the logged in user's info from the /oauth/userinfo endpoint.
     * See http://docs.telenordigital.com/apis/connect/id/authentication.html#authorization-server-user-information
     * for more details on the scope and claims preconditions needed in order to get the info
     * needed.
     *
     * @param userInfoCallback the callback that will be called on after the response is received
     * @throws ConnectNotInitializedException if no user is signed in.
     */
    public static void getUserInfo(Callback<UserInfo> userInfoCallback) {
        Validator.sdkInitialized();
        sConnectIdService.getUserInfo(userInfoCallback);
    }
}
