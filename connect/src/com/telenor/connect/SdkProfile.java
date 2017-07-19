package com.telenor.connect;

import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectTokensTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SdkProfile {

    Context getContext();
    HttpUrl getApiUrl();
    String getClientId();
    String getClientSecret();
    boolean isConfidentialClient();
    String getRedirectUri();
    ConnectIdService getConnectIdService();
    String getExpectedIssuer();
    List<String> getExpectedAudiences();
    Uri getAuthorizeUri(Map<String, String> parameters, List<String> locales);
    WellKnownAPI.WellKnownConfig getWellKnownConfig();
    boolean isInitialized();

    void onStartAuthorization(Map<String, String> parameters, OnStartAuthorizationCallback callback);

    interface OnStartAuthorizationCallback {
        void onSuccess();
        void onError();
    }

    void validateTokens(ConnectTokensTO tokens, Date serverTimestamp);
    void onFinishAuthorization(boolean success);
}
