package com.telenor.connect.id;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class ConnectIdService {

    private static ConnectIdService sInstance = null;

    private final ConnectAPI connectApi;
    private final TokenStore tokenStore;

    private ConnectTokens currentTokens;
    private IdToken idToken;

    private ConnectIdService() {
        final OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(IdToken.class, new IdTokenDeserializer())
                .create();

        final RequestInterceptor connectRetroFitInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        final RestAdapter sConnectAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(httpClient))
                .setEndpoint(ConnectSdk.getConnectApiUrl().toString())
                .setRequestInterceptor(connectRetroFitInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();

        connectApi = sConnectAdapter.create(ConnectAPI.class);
        tokenStore = new TokenStore(ConnectSdk.getContext());
    }

    public static synchronized ConnectIdService getInstance() {
        if (sInstance == null) {
            sInstance = new ConnectIdService();
        }
        return sInstance;
    }

    public String getAccessToken() {
        if (retrieveTokens() == null) {
            return null;
        }
        return retrieveTokens().accessToken;
    }

    public void getAccessTokenFromCode(
            final String authCode,
            final ConnectCallback callback) {
        connectApi.getAccessTokens(
                "authorization_code",
                authCode,
                ConnectSdk.getRedirectUri(),
                ConnectSdk.getClientId(),
                new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        Validator.validateTokens(connectTokens);
                        tokenStore.set(connectTokens);
                        currentTokens = connectTokens;
                        idToken = connectTokens.idToken;
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
        return retrieveTokens().refreshToken;
    }

    public void revokeTokens() {
        connectApi.revokeToken(
                ConnectSdk.getClientId(),
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
                ConnectSdk.getClientId(),
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
    }

    public void updateTokens(final ConnectCallback callback) {
        connectApi.refreshAccessTokens("refresh_token", getRefreshToken(),
                ConnectSdk.getClientId(), new Callback<ConnectTokens>() {
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

    public String getSubjectId() {
        if (idToken == null) {
            idToken = tokenStore.getIdToken();
        }

        return idToken != null ? idToken.getSubject() : null;
    }
}
