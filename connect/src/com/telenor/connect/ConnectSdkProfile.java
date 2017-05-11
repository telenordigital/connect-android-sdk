package com.telenor.connect;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.utils.ConnectUrlHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConnectSdkProfile extends AbstractSdkProfile {

    public static final String OAUTH_PATH = "oauth";

    private String clientId;
    private String redirectUri;

    public ConnectSdkProfile(
            Context context,
            boolean useStaging,
            boolean confidentialClient) {
        super(context, useStaging, confidentialClient);
    }

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
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public String getExpectedIssuer(String actualIssuer) {
        if (getWellKnownConfig() != null) {
            return getWellKnownConfig().getIssuer();
        }
        return getApiUrl() + OAUTH_PATH;
    }

    @Override
    public List<String> getExpectedAudiences(List<String> actualAudiences) {
        return Collections.singletonList(clientId);
    }

    @Override
    public Uri getAuthorizeUri(Map<String, String> parameters, List<String> locales) {
        return ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                getClientId(),
                getRedirectUri(),
                locales,
                getApiUrl())
                .buildUpon()
                .appendPath(OAUTH_PATH)
                .appendPath("authorize")
                .build();
    }

    @Override
    public void onStartAuthorization(Map<String, String> parameters, OnStartAuthorizationCallback callback) {
        initializeAndContinueAuthorizationFlow(callback);
    }

    @Override
    protected String getWellKnownEndpoint() {
        HttpUrl.Builder builder = getApiUrl().newBuilder();
        builder.addPathSegment(OAUTH_PATH);
        for (String pathSegment : WellKnownAPI.OPENID_CONFIGURATION_PATH.split("/")) {
            if (!TextUtils.isEmpty(pathSegment)) {
                builder.addPathSegment(pathSegment);
            }
        }
        return builder.build().toString();
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

}
