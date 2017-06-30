package com.telenor.connect;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.Validator;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ConnectSdkProfile extends AbstractSdkProfile {

    public static final String OAUTH_PATH = "oauth";

    private String clientId;
    private String redirectUri;
    private boolean useStaging;


    public ConnectSdkProfile(
            Context context,
            boolean useStaging,
            boolean confidentialClient) {
        super(context, confidentialClient);
        this.useStaging = useStaging;
    }

    @Override
    public HttpUrl getApiUrl() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(useStaging
                        ? "connect.staging.telenordigital.com"
                        : "connect.telenordigital.com")
                .build();
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
    public String getExpectedIssuer() {
        if (getWellKnownConfig() != null) {
            return getWellKnownConfig().getIssuer();
        }
        return getApiUrl() + OAUTH_PATH;
    }

    @Override
    public List<String> getExpectedAudiences() {
        return Collections.singletonList(clientId);
    }

    @Override
    public Uri getAuthorizeUri(ParametersHolder parameters, List<String> locales) {
        previewParameters(parameters);
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
    public String getWellKnownEndpoint() {
        HttpUrl.Builder builder = getApiUrl().newBuilder();
        builder.addPathSegment(OAUTH_PATH);
        for (String pathSegment : WellKnownAPI.OPENID_CONFIGURATION_PATH.split("/")) {
            if (!TextUtils.isEmpty(pathSegment)) {
                builder.addPathSegment(pathSegment);
            }
        }
        return applyUseStaginfOnEndpoint(builder.build().toString());
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

    @Override
    public boolean isInitialized() {
        return true;
    }

    private String applyUseStaginfOnEndpoint(String endpoint) {
        if (!useStaging) {
            return endpoint;
        }
        return endpoint.replace("connect.telenordigital.com", "connect.staging.telenordigital.com");
    }

    @Override
    public void validateTokens(ConnectTokensTO tokens, Date serverTimestamp) {
        super.validateTokens(tokens, serverTimestamp);
        Validator.notNullOrEmpty(tokens.getScope(), "scope");
        Validator.notNull(tokens.getExpiresIn(), "expires_in");
        Validator.notNullOrEmpty(tokens.getRefreshToken(), "refresh_token");
    }
}
