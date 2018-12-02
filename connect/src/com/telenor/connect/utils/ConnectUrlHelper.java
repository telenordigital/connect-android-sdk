package com.telenor.connect.utils;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import com.telenor.connect.BrowserType;
import com.telenor.connect.BuildConfig;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.headerenrichment.HeLogic;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

public class ConnectUrlHelper {

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String OAUTH_PATH = "oauth";
    private static final String HE_TOKEN_API_BASE_PATH = "id/extapi/v1/header-enrichment-token/";

    public static String getPageUrl(Bundle arguments) {
        if (ConnectUtils.LOGIN_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            if (arguments.getString(ConnectUtils.LOGIN_AUTH_URI) == null
                    || arguments.getString(ConnectUtils.LOGIN_AUTH_URI, "").isEmpty()) {
                throw new IllegalStateException("Required data missing for Login Action.");
            }
            return arguments.getString(ConnectUtils.LOGIN_AUTH_URI);
        }
        throw new IllegalStateException("An invalid action was used to start a Connect Activity.");
    }

    public static synchronized Uri getAuthorizeUri(
            Map<String, String> parameters, BrowserType browserType, String heToken) {
        if (ConnectSdk.getClientId() == null) {
            throw new ConnectException("Client ID not specified in application manifest.");
        }
        if (ConnectSdk.getRedirectUri() == null) {
            throw new ConnectException("Redirect URI not specified in application manifest.");
        }
        String scope = parameters.get("scope");
        if (scope == null || scope.isEmpty()) {
            throw new IllegalStateException("Cannot log in without scope tokens.");
        }
        if (heToken != null) {
            parameters.put("telenordigital_he_token", heToken);
        }
        handlePromptAndLogSessionId(parameters);
        parameters.put("state", ConnectSdk.getConnectStore().generateSessionStateParam());
        return ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                ConnectSdk.getClientId(),
                ConnectSdk.getRedirectUri(),
                ConnectSdk.getUiLocales(),
                getConnectApiUrl(), browserType)
                .buildUpon()
                .appendPath(OAUTH_PATH)
                .appendPath("authorize")
                .build();
    }

    public static HttpUrl getConnectApiUrl(boolean useStaging) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(useStaging
                        ? "connect.staging.telenordigital.com"
                        : "connect.telenordigital.com")
                .build();
    }

    private static void handlePromptAndLogSessionId(Map<String, String> parameters) {
        if (TextUtils.isEmpty(parameters.get("prompt")) && !HeLogic.isCellularDataNetworkConnected()) {
            parameters.put("prompt", "no_seam");
        }
        if (TextUtils.isEmpty(parameters.get("log_session_id"))) {
            parameters.put("log_session_id", ConnectSdk.getLogSessionId());
        }
    }

    public static HttpUrl getConnectApiUrl() {
        return getConnectApiUrl(ConnectSdk.useStaging());
    }

    public static Uri getAuthorizeUriStem(
            Map<String, String> parameters,
            String clientId,
            String redirectUri,
            List<String> locales,
            HttpUrl basePath,
            BrowserType browserType) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("response_type", "code");
        authParameters.put("client_id", clientId);
        authParameters.put("redirect_uri", redirectUri);
        authParameters.put("ui_locales", TextUtils.join(" ", locales));
        authParameters.put("telenordigital_sdk_version", getVersionParam(browserType));

        authParameters.putAll(parameters);

        Uri.Builder builder = new Uri.Builder();
        builder
                .scheme(basePath.scheme())
                .authority(basePath.host());
        for (Map.Entry<String, String> entry : authParameters.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private static String getVersionParam(BrowserType browserType) {
        return String.format("android_v%s_%s_%s",
                BuildConfig.VERSION_NAME,
                Build.VERSION.RELEASE,
                browserType != null ? browserType.getVersionString() : "not-defined");
    }

    public static String getHeApiUrl(boolean useStaging, String logSessionId) {
        HttpUrl connectApiSchemeAndHost = ConnectUrlHelper.getConnectApiUrl(useStaging);
        return connectApiSchemeAndHost
                + HE_TOKEN_API_BASE_PATH
                + logSessionId;
    }

    public static String getSubmitPinUrl(String pin) {
        String obfuscatedPin = getObfuscatedPin(pin, ConnectSdk.getLogSessionId());
        return ConnectUrlHelper.getConnectApiUrl().newBuilder()
                .addPathSegments("id/submit-pin")
                .addQueryParameter("pin", obfuscatedPin)
                .build()
                .toString();
    }

    protected static String getObfuscatedPin(String pin, String logSessionId) {
        String combined = pin + ":" + logSessionId;
        byte[] bytes;
        try {
            bytes = combined.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported.", e);
        }
        byte[] encoded = Base64.encode(bytes, Base64.NO_WRAP);
        try {
            return new String(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported.", e);
        }
        // custom tabs automatically url encodes the result, so it's not done here, to avoid
        // double encoding it.
    }
}
