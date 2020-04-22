package com.telenor.connect.connectidexample;

import android.app.Application;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.IdProvider;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitialize(getApplicationContext(), IdProvider.GP_ID, true, false);
    }
}
