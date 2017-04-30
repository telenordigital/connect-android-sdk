package com.telenor.connect;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.ConnectUrlHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConnectSdkProfile implements SdkProfile {

    private boolean useStaging;
    private String clientId;
    private boolean confidentialClient;
    private String redirectUri;

    private ConnectIdService connectIdService;

    @Override
    public HttpUrl getApiUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        builder.scheme("https");
        builder.host(useStaging
                ? "connect.staging.telenordigital.com"
                : "connect.telenordigital.com");
        return builder.build();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return null;
    }

    @Override
    public boolean isConfidentialClient() {
        return confidentialClient;
    }


    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public ConnectIdService getConnectIdService() {
        return connectIdService;
    }

    @Override
    public String getExpectedIssuer(String actualIssuer) {
        return getApiUrl() + ConnectUrlHelper.OAUTH_PATH;
    }

    @Override
    public List<String> getExpectedAudiences(List<String> actualAudiences) {
        return Arrays.asList(clientId);
    }

    @Override
    public DoNext onAuthorize(Map<String, String> parameters) {
        return DoNext.proceed;
    }

    public void setUseStaging(boolean useStaging) {
        this.useStaging = useStaging;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setConfidentialClient(boolean confidentialClient) {
        this.confidentialClient = confidentialClient;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public void setConnectIdService(ConnectIdService connectIdService) {
        this.connectIdService = connectIdService;
    }
}
