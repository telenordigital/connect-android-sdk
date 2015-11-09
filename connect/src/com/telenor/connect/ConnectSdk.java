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
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ConnectSdk {
    private static String sClientId;
    private static Context sContext;
    private static String sLastAuthState;
    private static ArrayList<Locale> sLocales;
    private static String sPaymentCancelUri;
    private static String sPaymentSuccessUri;
    private static String sRedirectUri;
    private static boolean sSdkInitialized = false;
    private static boolean sUseStaging = false;

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CLIENT_ID_PROPERTY = "com.telenor.connect.CLIENT_ID";

    /**
     * The key to enable payment in the Android manifest.
     */
    public static final String PAYMENT_ENABLED_PROPERTY = "com.telenor.connect.PAYMENT_ENABLED";

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

    public static synchronized void authenticate(Activity activity, String... scopeTokens) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("scope", TextUtils.join(" ", scopeTokens));
        authenticate(activity, parameters);
    }

    public static synchronized void authenticate(
            Activity activity,
            Map<String, String> parameters) {
        Intent intent = new Intent();
        intent.setClass(getContext(), ConnectActivity.class);
        intent.setAction(ConnectUtils.LOGIN_ACTION);
        intent.putExtra(ConnectUtils.LOGIN_AUTH_URI,
                getAuthorizeUri(parameters).toString());
        activity.startActivityForResult(intent, 1);
    }

    public static synchronized String getAccessToken() {
        return ConnectIdService.getInstance().getAccessToken();
    }

    public static synchronized void getAccessTokenFromCode(String code, ConnectCallback callback) {
        ConnectIdService.getAccessTokenFromCode(code, callback);
    }

    public static synchronized Uri getAuthorizeUri(Map<String, String> parameters) {
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

        Map<String, String> authParameters = new HashMap<String, String>();
        authParameters.put("response_type", "code");
        authParameters.put("client_id", ConnectSdk.getClientId());
        authParameters.put("redirect_uri", ConnectSdk.getRedirectUri());
        authParameters.put("ui_locales", TextUtils.join(" ", getUiLocales()));

        authParameters.putAll(parameters);

        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(getConnectApiUrl().toString())
                .appendPath("oauth")
                .appendPath("authorize");
        for (Map.Entry<String, String> entry : authParameters.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static HttpUrl getConnectApiUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme("https");
        builder.host(sUseStaging == true
                ? "connect.staging.telenordigital.com"
                : "connect.telenordigital.com");
        return builder.build();
    }

    public static Context getContext() {
        Validator.SdkInitialized();
        return sContext;
    }

    public static String getClientId() {
        Validator.SdkInitialized();
        return sClientId;
    }

    public static String getLastAuthenticationState() {
        Validator.SdkInitialized();
        return sLastAuthState;
    }

    public static ArrayList<Locale> getLocales() {
        Validator.SdkInitialized();
        return sLocales;
    }

    public static String getPaymentCancelUri() {
        Validator.SdkInitialized();
        return sPaymentCancelUri;
    }

    public static String getPaymentSuccessUri() {
        Validator.SdkInitialized();
        return sPaymentSuccessUri;
    }

    public static String getRedirectUri() {
        Validator.SdkInitialized();
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
        Validator.SdkInitialized();
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

    public static synchronized boolean isInitialized() {
        return sSdkInitialized;
    }

    public static void logout() {
        ConnectIdService.getInstance().revokeTokens();
    }

    public static synchronized void sdkInitialize(Context context) {
        if (sSdkInitialized == true) {
            return;
        }

        Validator.notNull(context, "context");
        ConnectSdk.sContext = context;
        ConnectSdk.loadConnectConfig(ConnectSdk.sContext);

        sSdkInitialized = true;
    }

    public static void setLocales(Locale... locales) {
        sLocales = new ArrayList<Locale>(Arrays.asList(locales));
    }

    public static void setLocales(ArrayList<Locale> locales) {
        sLocales = locales;
    }

    public static void updateTokens(ConnectCallback callback) {
        ConnectIdService.getInstance().updateTokens(callback);
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
}
