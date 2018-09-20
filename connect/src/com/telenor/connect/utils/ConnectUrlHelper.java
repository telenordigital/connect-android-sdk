package com.telenor.connect.utils;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.telenor.connect.BrowserType;
import com.telenor.connect.BuildConfig;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

public class ConnectUrlHelper {

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String OAUTH_PATH = "oauth";

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
            Map<String, String> parameters, BrowserType browserType) {
        if (ConnectSdk.getClientId() == null) {
            throw new ConnectException("Client ID not specified in application manifest.");
        }
        if (ConnectSdk.getRedirectUri() == null) {
            throw new ConnectException("Redirect URI not specified in application manifest.");
        }
        if (parameters.get("scope") == null || parameters.get("scope").isEmpty()) {
            throw new IllegalStateException("Cannot log in without scope tokens.");
        }
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
}
