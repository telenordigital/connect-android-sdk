package com.telenor.connect.mobileconnectexample;

import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

public class ExampleOperatorDiscoveryConfig implements OperatorDiscoveryConfig {

    private static final String OD_ENDPOINT = "https://discover.mobileconnect.io/gsma/v2/discovery";
    private static final String OD_CLIENT_ID = "abce79a0-5a73-488e-9389-4bc7f2685b5b";
    private static final String OD_CLIENT_SECRET = "923901ca-6977-4f90-b18a-34da0c5c7b69";
    private static final String OD_REDIRECT_URI = "https://localhost:8443/";

    @Override
    public String getOperatorDiscoveryClientId() {
        return OD_CLIENT_ID;
    }

    @Override
    public String getOperatorDiscoveryClientSecret() {
        return OD_CLIENT_SECRET;
    }

    @Override
    public String getOperatorDiscoveryRedirectUri() {
        return OD_REDIRECT_URI;
    }

    @Override
    public String getOperatorDiscoveryEndpoint() {
        return OD_ENDPOINT;
    }
}
