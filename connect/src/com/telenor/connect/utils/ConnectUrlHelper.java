package com.telenor.connect.utils;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectUrlHelper {

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String URL_ARGUMENT = "com.telenor.connect.URL_ARGUMENT";
    public static final String OAUTH_PATH = "oauth";

    public static String getPageUrl(Bundle arguments, Activity activity) {
        if (ConnectUtils.PAYMENT_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            return arguments.getString(URL_ARGUMENT);
        } else if (ConnectUtils.LOGIN_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            if (activity == null
                    || activity.getIntent() == null
                    || activity.getIntent()
                    .getStringExtra(ConnectUtils.LOGIN_AUTH_URI) == null
                    || activity.getIntent()
                    .getStringExtra(ConnectUtils.LOGIN_AUTH_URI).isEmpty()) {
                throw new IllegalStateException("Required data missing for Login Action.");
            }
            return activity.getIntent().getStringExtra(ConnectUtils.LOGIN_AUTH_URI);
        }
        throw new IllegalStateException("An invalid action was used to start a Connect Activity.");
    }

    public static Uri getAuthorizeUri(
            Map<String, String> parameters,
            String clientId,
            String redirectUri,
            ArrayList<String> locales,
            HttpUrl basePath) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("response_type", "code");
        authParameters.put("client_id", clientId);
        authParameters.put("redirect_uri", redirectUri);
        authParameters.put("ui_locales", TextUtils.join(" ", locales));
        authParameters.put("telenordigital_sdk_version", BuildConfig.VERSION_NAME);

        authParameters.putAll(parameters);

        Uri.Builder builder = new Uri.Builder();
        builder
                .scheme(basePath.scheme())
                .authority(basePath.host())
                .appendPath(OAUTH_PATH)
                .appendPath("authorize");
        for (Map.Entry<String, String> entry : authParameters.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
