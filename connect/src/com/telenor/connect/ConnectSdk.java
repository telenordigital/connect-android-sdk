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
    private static boolean sPaymentEnabled = false;
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

    public static synchronized Uri getAuthorizeUri(Map<String, String> parameters) {
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
        Validator.PaymentEnabled();

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

    public static synchronized boolean isPaymentEnabled() {
        return sPaymentEnabled;
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

        if (sClientId == null) {
            Object clientIdObject = ai.metaData.get(CLIENT_ID_PROPERTY);
            if (clientIdObject instanceof String) {
                String clientIdString = (String) clientIdObject;
                sClientId = clientIdString;
            } else {
                throw new ConnectException("Client Ids cannot be directly placed in the " +
                        "manifest. They must be placed in the string resource file.");
            }
        }

        Object paymentEnabledObject = ai.metaData.get(PAYMENT_ENABLED_PROPERTY);
        if (paymentEnabledObject instanceof Boolean) {
            sPaymentEnabled = (Boolean) paymentEnabledObject;
        }

        if (isPaymentEnabled()) {
            if (sPaymentCancelUri == null) {
                Object paymentCancelUriObject = ai.metaData.get(PAYMENT_CANCEL_URI_PROPERTY);
                if (paymentCancelUriObject instanceof String) {
                    String paymentCancelUriString = (String) paymentCancelUriObject;
                    sPaymentCancelUri = paymentCancelUriString;
                } else {
                    throw new ConnectException("Payment Cancel URIs cannot be directly placed in " +
                            "the manifest. They must be placed in the string resource file.");
                }
            }

            if (sPaymentSuccessUri == null) {
                Object paymentSuccessUriObject = ai.metaData.get(PAYMENT_SUCCESS_URI_PROPERTY);
                if (paymentSuccessUriObject instanceof String) {
                    String paymentSuccessUriString = (String) paymentSuccessUriObject;
                    sPaymentSuccessUri = paymentSuccessUriString;
                } else {
                    throw new ConnectException("Payment Success URIs cannot be directly placed " +
                            "in the manifest. They must be placed in the string resource file.");
                }
            }
        }

        if (sRedirectUri == null) {
            Object redirectUriObject = ai.metaData.get(REDIRECT_URI_PROPERTY);
            if (redirectUriObject instanceof String) {
                String redirectUriString = (String) redirectUriObject;
                sRedirectUri = redirectUriString;
            } else {
                throw new ConnectException("Redirect URIs cannot be directly placed in the " +
                        "manifest. They must be placed in the string resource file.");
            }
        }

        Object useStagingObject = ai.metaData.get(USE_STAGING_PROPERTY);
        if (useStagingObject instanceof Boolean) {
            sUseStaging = (Boolean) useStagingObject;
        }
    }
}
