package com.telenor.connect;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;

import java.util.List;
import java.util.Map;

public interface SdkProfile {

    HttpUrl getApiUrl();
    String getClientId();
    String getClientSecret();
    boolean isConfidentialClient();
    String getRedirectUri();
    ConnectIdService getConnectIdService();
    String getExpectedIssuer(String actualIssuer);
    List<String> getExpectedAudiences(List<String> actualAudiences);

    enum DoNext {proceed, cancel};
    DoNext onAuthorize(Map<String, String> parameters);
}
