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
                        .clientId("abce79a0-5a73-488e-9389-4bc7f2685b5b")
                        .clientSecret("923901ca-6977-4f90-b18a-34da0c5c7b69")
                        .redirectUri("https://localhost:8443/")
                        .build());
    }
}
