package com.telenor.connect.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectIdService;

public class ConnectUtils {
    public static final String LOGIN_ACTION = "com.telenor.connect.LOGIN_ACTION";
    public static final String PAYMENT_ACTION = "com.telenor.connect.PAYMENT_ACTION";

    public static String parseAuthCode(String callbackUrl) {
        Validator.notNullOrEmpty(callbackUrl, "callbackUrl");

        Uri uri = Uri.parse(callbackUrl);
        return uri.getQueryParameter("code");
    }

    public static void sendTokenStateChanged(boolean state) {
        Validator.SdkInitialized();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(
                ConnectSdk.getContext());
        Intent intent = new Intent(ConnectSdk.ACTION_LOGIN_STATE_CHANGED);
        intent.putExtra(ConnectIdService.EXTRA_LOGIN_STATE, state);
        localBroadcastManager.sendBroadcast(intent);
    }
}
