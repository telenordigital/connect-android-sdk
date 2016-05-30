package com.telenor.connect.id;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;

public class ActivityFinisherConnectCallback implements ConnectCallback {

    private final Activity activity;
    private final Gson gson = new Gson();

    public ActivityFinisherConnectCallback(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onSuccess(Object successData) {
        ConnectTokens connectTokens = (ConnectTokens) successData;
        Intent data = new Intent();
        String json = gson.toJson(connectTokens);
        data.putExtra(ConnectSdk.EXTRA_CONNECT_TOKENS, json);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    @Override
    public void onError(Object errorData) {
        Log.e(ConnectUtils.LOG_TAG, errorData.toString());
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }
}
