package com.telenor.connect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.List;

public final class ConnectSdk {
    private static String sAcrValue;
    private static String sClientId;
    private static ConnectIdService sConnectIdService;
    private static Context sContext;
    private static String sRedirectUri;
    private static List<String> sScopes;
    private static boolean sSdkInitialized = false;
    private static boolean sUseStaging = false;

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CLIENT_ID_PROPERTY = "com.telenor.connect.CLIENT_ID";

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

    public static synchronized void sdkInitialize(Context context) {
        Validator.notNull(context, "context");
        ConnectSdk.sContext = context;
        ConnectSdk.loadConnectConfig(ConnectSdk.sContext);

        ConnectSdk.sConnectIdService = new ConnectIdService();
        ConnectSdk.setAcrValue("1");
        sScopes = new ArrayList<>();
        sScopes.add("profile");
        sSdkInitialized = true;
    }

    public static synchronized String getAccessToken() {
        return getConnectIdService().getAccessToken();
    }

    public static void addScope(String scope) {
        Validator.SdkInitialized();

        sScopes.add(scope);
    }

    public static String getAcrValue() {
        Validator.SdkInitialized();

        return sAcrValue;
    }

    public static HttpUrl getConnectApiUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme("https");
        builder.host(sUseStaging == true
                ? "connect.staging.telenordigital.com"
                : "connect.telenordigital.com");
        return builder.build();
    }

    public static ConnectIdService getConnectIdService() {
        Validator.SdkInitialized();
        return sConnectIdService;
    }

    public static Context getContext() {
        Validator.SdkInitialized();
        return sContext;
    }

    public static String getClientId() {
        Validator.SdkInitialized();
        return sClientId;
    }

    public static String getRedirectUri() {
        Validator.SdkInitialized();
        return sRedirectUri;
    }

    public static List<String> getScopes() {
        Validator.SdkInitialized();
        return sScopes;
    }

    public static void initializePayment(Context context) {
        Validator.SdkInitialized();

        Intent intent = new Intent();
        intent.setClass(ConnectSdk.getContext(), ConnectActivity.class);
        intent.setAction("PAYMENT");

        Activity activity = (Activity) context;
        activity.startActivityForResult(intent, 1);
    }

    public static synchronized boolean isInitialized() {
        return sSdkInitialized;
    }

    public static void logout() {
        getConnectIdService().revokeTokens();
    }

    public static void setAcrValue(String value) {
        sAcrValue = value;
    }

    public static void updateTokens() {
        getConnectIdService().updateTokens();
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
            return;
        }

        if (sClientId == null) {
            Object clientIdObject = ai.metaData.get(CLIENT_ID_PROPERTY);
            if (clientIdObject instanceof String) {
                String clientIdString = (String) clientIdObject;
                sClientId = clientIdString;
            } else {
                Log.e("ConnectSdk", "Client Ids cannot be directly placed in the manifest." +
                                "They must be placed in the string resource file.");
            }
        }

        if (sRedirectUri == null) {
            Object redirectUriObject = ai.metaData.get(REDIRECT_URI_PROPERTY);
            if (redirectUriObject instanceof String) {
                String redirectUriString = (String) redirectUriObject;
                sRedirectUri = redirectUriString;
            } else {
                Log.e("ConnectSdk", "Redirect URIs cannot be directly placed in the manifest." +
                        "They must be placed in the string resource file.");
            }
        }


        Object useStagingObject = ai.metaData.get(USE_STAGING_PROPERTY);
        if (useStagingObject instanceof Boolean) {
            sUseStaging = (Boolean) useStagingObject;
        }
    }
}
