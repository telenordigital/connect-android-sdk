package com.telenor.connect.id;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import java.util.Map;

public class AccessTokenCallback implements ConnectCallback {

    private final Activity activity;

    public AccessTokenCallback(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onSuccess(Object successData) {
        Validator.notNullOrEmpty(successData.toString(), "auth reponse");

        Map<String, String> authCodeData = (Map<String, String>) successData;
        if (ConnectSdk.isConfidentialClient()) {
            // When the client is confidential: exit here and return code and state.
            Intent intent = new Intent();
            for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();

        } else {
            ConnectIdService.getAccessTokenFromCode(authCodeData.get("code"),
                    new ConnectCallback() {
                        @Override
                        public void onSuccess(Object successData) {
                            activity.setResult(Activity.RESULT_OK);
                            activity.finish();
                        }

                        @Override
                        public void onError(Object errorData) {
                            Log.e(ConnectUtils.LOG_TAG, errorData.toString());
                            activity.setResult(Activity.RESULT_CANCELED);
                            activity.finish();
                        }
                    });
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
