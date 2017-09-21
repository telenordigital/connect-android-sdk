package com.telenor.connect.id;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectNotSignedInException;
import com.telenor.connect.ConnectRefreshTokenMissingException;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.HeadersDateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConnectIdService {

    private final ConnectAPI connectApi;
    private final ConnectStore connectStore;
    private final String redirectUrl;
    private final String clientId;

    private ConnectTokens currentTokens;
    private IdToken idToken;

    public ConnectIdService(
            ConnectStore connectStore, ConnectAPI connectApi, String clientId, String redirectUrl) {
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
        this.connectApi = connectApi;
        this.connectStore = connectStore;
    }

    public void getValidAccessToken(final AccessTokenCallback callback) {
        ConnectTokens connectTokens = retrieveTokens();
        if (connectTokens == null) {
            throw new ConnectRefreshTokenMissingException(
                    "retrieveTokens() returned null. Tokens are missing. Is the user signed in?");
        }

        if (connectTokens.accessTokenHasExpired()) {
            updateTokens(callback);
            return;
        }

        callback.onSuccess(connectTokens.getAccessToken());
    }

    public String getAccessToken() {
        ConnectTokens connectTokens = retrieveTokens();
        if (connectTokens == null) {
            return null;
        }

        return connectTokens.getAccessToken();
    }

    public Date getAccessTokenExpirationTime() {
        ConnectTokens connectTokens = retrieveTokens();
        if (connectTokens == null) {
            return null;
        }

        return connectTokens.getExpirationDate();
    }

    public void getAccessTokenFromCode(
            final String authCode,
            final ConnectCallback callback) {
        connectApi.getAccessTokens(
                "authorization_code",
                authCode,
                redirectUrl,
                clientId,
                new Callback<ConnectTokensTO>() {
                    @Override
                    public void success(ConnectTokensTO connectTokensTO, Response response) {
                        Date serverTimestamp
                                = HeadersDateUtil.extractDate(response.getHeaders());
                        ConnectTokens connectTokens
                                = new ConnectTokens(connectTokensTO, serverTimestamp);
                        connectStore.set(connectTokens);
                        connectStore.clearSessionStateParam();
                        currentTokens = connectTokens;
                        idToken = connectTokens.getIdToken();
                        ConnectUtils.sendTokenStateChanged(true);
                        if (callback != null) {
                            callback.onSuccess(connectTokens);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        clearTokensAndNotify();
                        if (callback != null) {
                            Map<String, String> errorParams = new HashMap<>();
                            errorParams.put("error", error.toString());
                            callback.onError(errorParams);
                        }
                    }
                });

    }

    private void clearTokensAndNotify() {
        connectStore.clear();
        currentTokens = null;
        idToken = null;
        ConnectUtils.sendTokenStateChanged(false);
    }

    private String getRefreshToken() {
        ConnectTokens connectTokens = retrieveTokens();
        if (connectTokens == null) {
            return null;
        }
        return connectTokens.getRefreshToken();
    }

    public void revokeTokens(Context context) {
        String accessToken = getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            revokeAccessToken(accessToken);
        }
        String refreshToken = getRefreshToken();
        if (!TextUtils.isEmpty(refreshToken)) {
            revokeRefreshToken(refreshToken);
        }

        clearTokensAndNotify();
        clearCookies(context);
    }

    private void revokeAccessToken(String accessToken) {
        revokeToken(accessToken, "access");
    }

    private void revokeRefreshToken(String refreshToken) {
        revokeToken(refreshToken, "refresh");
    }

    private void revokeToken(final String token, final String descriptor) {
        connectApi.revokeToken(
                clientId,
                token,
                new ResponseCallback() {
                    @Override
                    public void success(Response response) {
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(ConnectUtils.LOG_TAG, "Failed to call revoke " + descriptor +
                                " token on API. token=" + token , error);
                    }
                });
    }

    private void clearCookies(Context context) {
        final CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else {
            CookieSyncManager.createInstance(context);
            cookieManager.removeAllCookie();
        }
    }

    public void logOut(final Context context) {
        final String accessToken = getAccessToken();
        final String refreshToken = getRefreshToken();

        if (accessToken == null || refreshToken == null) {
            Log.w(ConnectUtils.LOG_TAG, "Trying to log out when user is already logged out.");
            revokeTokens(context);
            return;
        }

        updateTokens(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                callLogOutApiEndpoint(accessToken);
                revokeTokens(context);
            }

            @Override
            public void onError(Object errorData) {
                revokeTokens(context);
            }
        });
    }

    private void callLogOutApiEndpoint(final String accessToken) {
        String auth = "Bearer " + accessToken;
        connectApi.logOut(auth, new ResponseCallback() {
            @Override
            public void success(Response response) {

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(ConnectUtils.LOG_TAG, "Failed to call logout with access token on API. accessToken=" + accessToken , error);
            }
        });
    }

    public void updateTokens(final AccessTokenCallback callback) {
        final String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            throw new ConnectRefreshTokenMissingException("Refresh token missing, " +
                    "can't update tokens.");
        }
        connectApi.refreshAccessTokens("refresh_token", refreshToken,
                clientId, new Callback<ConnectTokensTO>() {
                    @Override
                    public void success(ConnectTokensTO connectTokensTO, Response response) {
                        Date serverTimestamp
                                = HeadersDateUtil.extractDate(response.getHeaders());
                        ConnectTokens connectTokens
                                = new ConnectTokens(connectTokensTO, serverTimestamp);
                        connectStore.update(connectTokens);
                        currentTokens = connectTokens;
                        callback.onSuccess(connectTokens.getAccessToken());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error != null
                                && error.getResponse() != null
                                && error.getResponse().getStatus() >= 400
                                && error.getResponse().getStatus() < 500) {
                            clearTokensAndNotify();
                        }
                        callback.onError(error);
                    }
                });
    }

    private ConnectTokens retrieveTokens() {
        if (currentTokens == null) {
            currentTokens = connectStore.get();
        }
        return currentTokens;
    }

    public IdToken getIdToken() {
        if (idToken == null) {
            idToken = connectStore.getIdToken();
        }
        return idToken;
    }

    public void getUserInfo(Callback<UserInfo> userInfoCallback)
            throws ConnectNotSignedInException {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new ConnectNotSignedInException(
                    "No user is signed in. accessToken="+accessToken);
        }
        final String auth = "Bearer " + accessToken;
        connectApi.getUserInfo(auth, userInfoCallback);
    }

}