package com.telenor.connect.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectIdService;

import java.util.HashMap;
import java.util.Map;

public class ConnectUtils {
    public static final String LOG_TAG = "ConnectSDK";
    public static final String LOGIN_ACTION = "com.telenor.connect.LOGIN_ACTION";
    public static final String LOGIN_SCOPE_TOKENS = "com.telenor.connect.LOGIN_SCOPE_TOKENS";
    public static final String LOGIN_STATE = "com.telenor.connect.LOGIN_STATE";
    public static final String PAYMENT_ACTION = "com.telenor.connect.PAYMENT_ACTION";
    public static final String PREFERENCES_FILE = "com.telenor.connect.PREFERENCES_FILE";

    public static void parseAuthCode(String callbackUrl, ConnectCallback callback) {
        Validator.notNullOrEmpty(callbackUrl, "callbackUrl");

        Uri uri = Uri.parse(callbackUrl);
        if (uri.getQueryParameter("error") != null) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("error", uri.getQueryParameter("error"));
            errorParams.put("error_description", uri.getQueryParameter("error_description"));
            callback.onError(errorParams);
            return;
        }
        callback.onSuccess(uri.getQueryParameter("code"));
    }

    public static void sendTokenStateChanged(boolean state) {
        Validator.SdkInitialized();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(
                ConnectSdk.getContext());
        Intent intent = new Intent(ConnectSdk.ACTION_LOGIN_STATE_CHANGED);
        intent.putExtra(LOGIN_STATE, state);
        localBroadcastManager.sendBroadcast(intent);
    }
}
