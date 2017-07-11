package com.telenor.connect.utils;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.BuildConfig;
import com.telenor.connect.ParametersHolder;

import java.util.List;
import java.util.Map;

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
            ParametersHolder parameters,
            String clientId,
            String redirectUri,
            List<String> locales,
            HttpUrl basePath) {
        ParametersHolder authParameters = new ParametersHolder();
        authParameters.add("response_type", "code");
        authParameters.add("client_id", clientId);
        authParameters.add("redirect_uri", redirectUri);
        authParameters.add("ui_locales", TextUtils.join(" ", locales));
        authParameters.add("telenordigital_sdk_version", getVersionParam());

        authParameters.addAll(parameters);

        Uri.Builder builder = new Uri.Builder();
        builder
                .scheme(basePath.scheme())
                .authority(basePath.host());
        for (Map.Entry<String, String> entry : authParameters.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private static String getVersionParam() {
        return String.format("android_v%s_%s", BuildConfig.VERSION_NAME, Build.VERSION.RELEASE);
    }
}
