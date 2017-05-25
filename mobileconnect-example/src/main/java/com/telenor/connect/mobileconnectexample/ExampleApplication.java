package com.telenor.connect.mobileconnectexample;

import android.app.Application;

import com.telenor.connect.ConnectSdk;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitializeMobileConnect(
                getApplicationContext(),
                OperatorDiscoveryConfig
                        .builder()
                        .endpoint("https://discover.mobileconnect.io/gsma/v2/discovery")
                        .clientId("ac8eb92d-5d6b-4db7-b46f-730534386026")
                        .clientSecret("4af5f0dd-aa3c-420a-8790-2212476728c6")
                        .redirectUri("https://localhost:8443/")
                        .build());
    }
}
