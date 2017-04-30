package com.telenor.connect.mobileconnectexample;

import android.app.Application;

import com.telenor.connect.ConnectSdk;

public class ExampleApplication extends Application {

    private final String operatorDiscoveryEndpoint = "https://discover.mobileconnect.io/gsma/v2/discovery";
    private final String operatorDiscoveryClientId = "abce79a0-5a73-488e-9389-4bc7f2685b5b";
    private final String operatorDiscoveryClientSecret = "923901ca-6977-4f90-b18a-34da0c5c7b69";
    private final String operatorDiscoveryRedirectUri = "https://localhost:8443/";

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitializeMobileConnect(
                getApplicationContext(),
                new ExampleOperatorDiscoveryConfig());
    }
}
