package com.telenor.connect;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectTokensTO;

import java.util.Date;
import java.util.List;

import static com.telenor.connect.WellKnownAPI.WellKnownConfig;

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
    Uri getAuthorizeUri(ParametersHolder parameters, List<String> locales);
    WellKnownAPI.WellKnownConfig getWellKnownConfig();
    boolean isInitialized();

    String getLastAuthState();

    void initializeAuthorizationFlow(Activity activity,
                                     AuthFlowInitializationCallback callback);
    interface AuthFlowInitializationCallback {
        void onSuccess(Uri authorizeUri, WellKnownConfig wellKnowConfig);
        void onError(ConnectInitializationError error);
    }

    void validateTokens(ConnectTokensTO tokens, Date serverTimestamp);
    void onFinishAuthorization(boolean success);
}
