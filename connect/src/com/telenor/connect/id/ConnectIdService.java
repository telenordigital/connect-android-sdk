package com.telenor.connect.id;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectNotSignedInException;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConnectIdService {

    private final ConnectAPI connectApi;
    private final TokenStore tokenStore;
    private final String redirectUrl;
    private final String clientId;

    private ConnectTokens currentTokens;
    private IdToken idToken;

    public ConnectIdService(
            TokenStore tokenStore, ConnectAPI connectApi, String clientId, String redirectUrl) {
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
        this.connectApi = connectApi;
        this.tokenStore = tokenStore;
    }

    public String getAccessToken() {
        if (retrieveTokens() == null) {
            return null;
        }
        return retrieveTokens().getAccessToken();
    }

    public void getAccessTokenFromCode(
            final String authCode,
            final ConnectCallback callback) {
        connectApi.getAccessTokens(
                "authorization_code",
                authCode,
                redirectUrl,
                clientId,
                new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        Validator.validateTokens(connectTokens);
                        tokenStore.set(connectTokens);
                        currentTokens = connectTokens;
                        idToken = connectTokens.getIdToken();
                        ConnectUtils.sendTokenStateChanged(true);
                        if (callback != null) {
                            callback.onSuccess(connectTokens);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ConnectUtils.sendTokenStateChanged(false);
                        if (callback != null) {
                            callback.onError(error.toString());
                        }
                    }
                });
    }

    private String getRefreshToken() {
        return retrieveTokens().getRefreshToken();
    }

    public void revokeTokens(Context context) {
        connectApi.revokeToken(
                clientId,
                getAccessToken(),
                new ResponseCallback() {
            @Override
            public void success(Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(ConnectUtils.LOG_TAG, "Failed to call revoke access token on API", error);
            }
        });
        connectApi.revokeToken(
                clientId,
                getRefreshToken(),
                new ResponseCallback() {
            @Override
            public void success(Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(ConnectUtils.LOG_TAG, "Failed to call revoke refresh token on API", error);
            }
        });
        tokenStore.clear();
        currentTokens = null;
        idToken = null;
        ConnectUtils.sendTokenStateChanged(false);
        final CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else {
            CookieSyncManager.createInstance(context);
            cookieManager.removeAllCookie();
        }
    }

    public void updateTokens(final ConnectCallback callback) {
        connectApi.refreshAccessTokens("refresh_token", getRefreshToken(),
                clientId, new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        Validator.validateTokens(connectTokens);
                        tokenStore.update(connectTokens);
                        currentTokens = connectTokens;
                        callback.onSuccess(connectTokens);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error != null
                                && error.getResponse() != null
                                && error.getResponse().getStatus() >= 400
                                && error.getResponse().getStatus() < 500) {
                            tokenStore.clear();
                            currentTokens = null;
                        }
                        callback.onError(error);
                    }
                });
    }

    private ConnectTokens retrieveTokens() {
        if (currentTokens == null) {
            currentTokens = tokenStore.get();
        }
        return currentTokens;
    }

    public IdToken getIdToken() {
        if (idToken == null) {
            idToken = tokenStore.getIdToken();
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