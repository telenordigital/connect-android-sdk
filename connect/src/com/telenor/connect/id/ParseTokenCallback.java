package com.telenor.connect.id;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import java.util.Map;

public class ParseTokenCallback implements ConnectCallback {

    private final Activity activity;

    public ParseTokenCallback(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onSuccess(Object successData) {
        Validator.notNullOrEmpty(successData.toString(), "auth reponse");

        Map<String, String> authCodeData = (Map<String, String>) successData;
        if (ConnectSdk.isConfidentialClient()) {
            Validator.validateAuthenticationState(authCodeData.get("state"));
            Intent intent = new Intent();
            for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        } else {
            ConnectSdk.getAccessTokenFromCode(
                    authCodeData.get("code"), new ActivityFinisherConnectCallback(activity));
        }
    }

    @Override
    public void onError(Object errorData) {
        Log.e(ConnectUtils.LOG_TAG, errorData.toString());
        Intent intent = new Intent();
        Map<String, String> authCodeData = (Map<String, String>) errorData;
        for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }
}
