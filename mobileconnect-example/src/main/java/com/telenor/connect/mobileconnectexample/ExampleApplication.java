package com.telenor.connect.mobileconnectexample;

import android.app.Application;

import com.telenor.connect.ConnectSdk;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

public class ExampleApplication extends Application {

    private static String
            SANDBOX_OPERATOR_DISCOVERY_CLIENT_ID = "665be206-d399-4c2c-a22c-489ffc8eeaed";
    private static String
            SANDBOX_OPERATOR_DISCOVERY_SECRET = "4451fea5-7e03-4919-bd69-cfc31ef9caa4";
    private static String
            INTEGRATION_OPERATOR_DISCOVERY_CLIENT_ID = "ac8eb92d-5d6b-4db7-b46f-730534386026";
    private static String
            INTEGRATION_OPERATOR_DISCOVERY_SECRET = "4af5f0dd-aa3c-420a-8790-2212476728c6";
    private static String
            PRODUCTION_OPERATOR_DISCOVERY_CLIENT_ID = "abce79a0-5a73-488e-9389-4bc7f2685b5b";
    private static String
            PRODUCTION_OPERATOR_DISCOVERY_SECRET = "923901ca-6977-4f90-b18a-34da0c5c7b69";

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitializeMobileConnect(
                getApplicationContext(),
                OperatorDiscoveryConfig
                        .builder()
                        .endpoint("https://discover.mobileconnect.io/gsma/v2/discovery")
                        .clientId(INTEGRATION_OPERATOR_DISCOVERY_CLIENT_ID)
                        .clientSecret(INTEGRATION_OPERATOR_DISCOVERY_SECRET)
                        .redirectUri("https://localhost:8443/")
                        .build());
    }
}
