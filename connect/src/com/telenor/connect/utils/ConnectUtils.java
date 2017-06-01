package com.telenor.connect.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;

import java.util.HashMap;
import java.util.Map;

public class ConnectUtils {
    public static final String LOG_TAG = "ConnectSDK";
    public static final String LOGIN_ACTION = "com.telenor.connect.LOGIN_ACTION";
    public static final String LOGIN_AUTH_URI = "com.telenor.connect.LOGIN_AUTH_URI";
    public static final String LOGIN_STATE = "com.telenor.connect.LOGIN_STATE";
    public static final String PAYMENT_ACTION = "com.telenor.connect.PAYMENT_ACTION";
    public static final String CUSTOM_LOADING_SCREEN_EXTRA
            = "com.telenor.connect.CUSTOM_LOADING_SCREEN_EXTRA";
    public static final String IS_DIVERTED_TO_FETCH_INIT_URI
            = "com.telenor.connect.IS_DIVERTED_TO_FETCH_INIT_URI";
    public static final String PARAM_SNAPHOT_EXTRA = "com.telenor.connect.PARAM_SNAPSHOT";
    public static final String UI_LOCALES_SNAPHOT_EXTRA = "com.telenor.connect.UI_LOCALES_SNAPSHOT";
    public static final String WELL_KNOWN_CONFIG_EXTRA
            = "com.telenor.connect.WELL_KNOWN_CONFIG_EXTRA";

    public static void parseAuthCode(String callbackUrl, ConnectCallback callback) {
        Validator.notNullOrEmpty(callbackUrl, "callbackUrl");

        Uri uri = Uri.parse(callbackUrl);
        Validator.validateAuthenticationState(uri.getQueryParameter("state"));
        if (uri.getQueryParameter("error") != null) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("error", uri.getQueryParameter("error"));
            errorParams.put("error_description", uri.getQueryParameter("error_description"));
            callback.onError(errorParams);
            return;
        }

        Map<String, String> successParams = new HashMap<>();
        successParams.put("code", uri.getQueryParameter("code"));
        successParams.put("state", uri.getQueryParameter("state"));
        callback.onSuccess(successParams);
    }

    public static void sendTokenStateChanged(boolean state) {
        Validator.sdkInitialized();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(ConnectSdk.getContext());
        Intent intent = new Intent(ConnectSdk.ACTION_LOGIN_STATE_CHANGED);
        intent.putExtra(LOGIN_STATE, state);
        localBroadcastManager.sendBroadcast(intent);
    }
}
