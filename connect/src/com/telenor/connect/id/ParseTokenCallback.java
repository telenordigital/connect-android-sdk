package com.telenor.connect.id;

import android.util.Log;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import java.util.Map;

public class ParseTokenCallback implements ConnectCallback {

    private final ConnectCallback callback;
    private final String scopes;

    public ParseTokenCallback(ConnectCallback callback, String scopes) {
        this.callback = callback;
        this.scopes = scopes;
    }

    @Override
    public void onSuccess(Object successData) {
        Validator.notNullOrEmpty(successData.toString(), "auth reponse");
        Map<String, String> authCodeData = (Map<String, String>) successData;
        if (ConnectSdk.isConfidentialClient()) {
            callback.onSuccess(successData);
        } else {
            ConnectSdk.getAccessTokenFromCode(authCodeData.get("code"), scopes, callback);
        }
    }

    @Override
    public void onError(Object errorData) {
        Log.e(ConnectUtils.LOG_TAG, errorData.toString());
        callback.onError(errorData);
    }
}
