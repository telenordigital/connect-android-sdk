package com.telenor.connect;

import android.app.Activity;
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

    void initializeAuthorizationFlow(final Activity activity,
                                     final SdkCallback callback);
    void fetchWellknownConfig(final SdkCallback callback);

    interface SdkCallback {
        void onSuccess();
        void onError();
    }

    void onFinishAuthorization(boolean success);

    void setValue(final String key, final String value);
    String getValue(final String key);
}
