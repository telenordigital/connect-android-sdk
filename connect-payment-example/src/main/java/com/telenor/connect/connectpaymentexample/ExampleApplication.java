package com.telenor.connect.connectpaymentexample;

import android.app.Application;

import com.telenor.connect.ConnectSdk;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitialize(getApplicationContext());
    }
}
