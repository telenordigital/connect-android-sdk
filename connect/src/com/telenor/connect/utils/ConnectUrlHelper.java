package com.telenor.connect.utils;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.telenor.connect.BrowserType;
import com.telenor.connect.BuildConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

public class ConnectUrlHelper {

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String URL_ARGUMENT = "com.telenor.connect.URL_ARGUMENT";

    public static String getPageUrl(Bundle arguments) {
        if (ConnectUtils.PAYMENT_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            return arguments.getString(URL_ARGUMENT);
        }

        if (ConnectUtils.LOGIN_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            if (arguments.getString(ConnectUtils.LOGIN_AUTH_URI) == null
                    || arguments.getString(ConnectUtils.LOGIN_AUTH_URI, "").isEmpty()) {
                throw new IllegalStateException("Required data missing for Login Action.");
            }
            return arguments.getString(ConnectUtils.LOGIN_AUTH_URI);
        }
        throw new IllegalStateException("An invalid action was used to start a Connect Activity.");
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
