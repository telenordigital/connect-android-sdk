package com.telenor.connect.id;

import android.app.Activity;
import android.util.Log;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.utils.ConnectUtils;

public class ActivityFinisherConnectCallback implements ConnectCallback {

    private final Activity activity;

    public ActivityFinisherConnectCallback(final Activity activity) {
        this.activity = activity;
    }

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
}
