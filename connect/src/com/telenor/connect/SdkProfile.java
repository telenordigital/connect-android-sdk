package com.telenor.connect;

import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;

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

    void initializeFromUri(
            Map<String, String> parameters,
            List<String> uiLocales,
            Uri initFrom,
            OnDeliverAuthorizationUriCallback callback);

    void onStartAuthorization(
            Map<String, String> parameters,
            List<String> uiLocales,
            OnStartAuthorizationCallback callback);

    interface OnDeliverAuthorizationUriCallback {
        void onSuccess(Uri authorizationStartUri);
        void onError();
    }

    interface OnStartAuthorizationCallback extends OnDeliverAuthorizationUriCallback {
        void onDivert(Uri diversionUri);
    }
}
