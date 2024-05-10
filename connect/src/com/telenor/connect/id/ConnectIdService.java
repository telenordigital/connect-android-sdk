package com.telenor.connect.id;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectNotSignedInException;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.HeadersDateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            callback.noSignedInUser();
            return;
        }

        if (connectTokens.accessTokenHasExpired()) {
            updateTokens(callback);
            return;
        }

        callback.success(connectTokens.getAccessToken());
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
            final String codeChallenge,
            final String scopes,
            final ConnectCallback callback) {
        connectApi.getAccessTokenFromCode(
                "authorization_code",
                authCode,
                redirectUrl,
                clientId,
                codeChallenge,
                scopes)
                .enqueue(new Callback<ConnectTokensTO>() {
                    @Override
                    public void onResponse(Call<ConnectTokensTO> call, Response<ConnectTokensTO> response) {
                        if (response.isSuccessful()) {
                            Date serverTimestamp = HeadersDateUtil.extractDate(response.headers());
                            ConnectTokens connectTokens;
                            try {
                                connectTokens = new ConnectTokens(response.body(), serverTimestamp);
                            } catch (ConnectException exception) {
                                onFailure(call, exception);
                                return;
                            }
                            connectStore.set(connectTokens);
                            connectStore.clearSessionStateParam();
                            currentTokens = connectTokens;
                            idToken = connectTokens.getIdToken();
                            ConnectUtils.sendTokenStateChanged(true);
                            if (callback != null) {
                                callback.onSuccess(connectTokens);
                            }
                        } else {
                            clearTokensAndNotify();
                            if (callback != null) {
                                callback.onError(response.errorBody());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ConnectTokensTO> call, Throwable error) {
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

    private void revokeTokens(Context context) {
        clearTokensAndNotify();
        clearCookies(context);
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
        final IdToken idToken = getIdToken();

        if (idToken == null) {
            Log.w(ConnectUtils.LOG_TAG, "Trying to log out when user is already logged out.");
            revokeTokens(context);
            return;
        }

        callLogOutApiEndpoint(idToken.getSerializedSignedJwt(), new ConnectCallback() {
            @Override
            public void onSuccess(Object successData) {
                revokeTokens(context);
            }

            @Override
            public void onError(Object errorData) {
                revokeTokens(context);
            }
        });
    }

    private void callLogOutApiEndpoint(final String serializedSignedJwt, final ConnectCallback callback) {
        connectApi.logOut(serializedSignedJwt).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    Log.e(ConnectUtils.LOG_TAG, "Failed to call logout with id token on API. accessToken=" + serializedSignedJwt);
                    callback.onError(null);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable error) {
                Log.e(ConnectUtils.LOG_TAG, "Failed to call logout with access id on API. accessToken=" + serializedSignedJwt , error);
                callback.onError(error);
            }
        });
    }

    public void updateTokens(final AccessTokenCallback callback) {
        final String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            callback.noSignedInUser();
            return;
        }
        connectApi.refreshAccessTokens("refresh_token", refreshToken,
                clientId).enqueue(new Callback<ConnectTokensTO>() {
                    @Override
                    public void onResponse(Call<ConnectTokensTO> call, Response<ConnectTokensTO> response) {
                        if (response.isSuccessful()) {
                            Date serverTimestamp = HeadersDateUtil.extractDate(response.headers());
                            ConnectTokens connectTokens;
                            try {
                                connectTokens = new ConnectTokens(response.body(), serverTimestamp);
                            } catch (ConnectException exception) {
                                onFailure(call, exception);
                                return;
                            }
                            connectStore.update(connectTokens);
                            currentTokens = connectTokens;
                            callback.success(connectTokens.getAccessToken());
                        } else {
                            boolean signOutUser = response.code() >= 400 && response.code() < 500;
                            if (signOutUser) {
                                clearTokensAndNotify();
                            }
                            callback.unsuccessfulResult(response, signOutUser);
                        }
                    }

                    @Override
                    public void onFailure(Call<ConnectTokensTO> call, Throwable error) {
                        callback.failure(call, error);
                    }
                });
    }

    public void updateLegacyTokens(final AccessTokenCallback callback) {
        final String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            callback.noSignedInUser();
            return;
        }
        connectApi.refreshLegacyAccessTokens("refresh_token", refreshToken,
                clientId).enqueue(new Callback<ConnectTokensTO>() {
            @Override
            public void onResponse(Call<ConnectTokensTO> call, Response<ConnectTokensTO> response) {
                if (response.isSuccessful()) {
                    Date serverTimestamp = HeadersDateUtil.extractDate(response.headers());
                    ConnectTokens connectTokens;
                    try {
                        connectTokens = new ConnectTokens(response.body(), serverTimestamp);
                    } catch (ConnectException exception) {
                        onFailure(call, exception);
                        return;
                    }
                    connectStore.update(connectTokens);
                    currentTokens = connectTokens;
                    callback.success(connectTokens.getAccessToken());
                } else {
                    boolean signOutUser = response.code() >= 400 && response.code() < 500;
                    if (signOutUser) {
                        clearTokensAndNotify();
                    }
                    callback.unsuccessfulResult(response, signOutUser);
                }
            }

            @Override
            public void onFailure(Call<ConnectTokensTO> call, Throwable error) {
                callback.failure(call, error);
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
}
