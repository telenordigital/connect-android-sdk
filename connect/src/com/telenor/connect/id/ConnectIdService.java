package com.telenor.connect.id;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
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

    private static final String PREFERENCE_KEY_CONNECT_TOKENS = "CONNECT_TOKENS";
    private static final Gson preferencesGson = new Gson();

    private static ConnectAPI sConnectApi;
    private static ConnectIdService instance = null;
    private static ConnectTokens sCurrentTokens;

    private ConnectIdService() {
        final HttpUrl connectUrl = ConnectSdk.getConnectApiUrl();

        final OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(IdToken.class, new IdTokenDeserializer())
                .create();

        final RequestInterceptor sConnectRetroFitInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        final RestAdapter sConnectAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(httpClient))
                .setEndpoint(connectUrl.toString())
                .setRequestInterceptor(sConnectRetroFitInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();

        sConnectApi = sConnectAdapter.create(ConnectAPI.class);
    }

    public static synchronized ConnectIdService getInstance() {
        if (instance == null) {
            instance = new ConnectIdService();
        }
        return instance;
    }

    public static String getAccessToken() {
        if (retrieveTokens() == null) {
            return null;
        }
        return retrieveTokens().accessToken;
    }

    public static void getAccessTokenFromCode(
            final String authCode,
            final ConnectCallback callback) {
        getConnectApi().getAccessTokens(
                "authorization_code",
                authCode,
                ConnectSdk.getRedirectUri(),
                ConnectSdk.getClientId(),
                new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        Validator.validateTokens(connectTokens);
                        storeAndSetTokens(connectTokens);
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

    private static ConnectAPI getConnectApi() {
        if (sConnectApi == null) {
            getInstance();
        }
        return sConnectApi;
    }

    private static String getRefreshToken() {
        return retrieveTokens().refreshToken;
    }

    public static void revokeTokens() {
        getConnectApi().revokeToken(
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
        getConnectApi().revokeToken(
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
        deleteStoredTokens();
        ConnectUtils.sendTokenStateChanged(false);
    }

    public static void storeAndSetTokens(ConnectTokens connectTokens) {
        SharedPreferences prefs = ConnectSdk.getContext()
                .getSharedPreferences(ConnectUtils.PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();

        String jsonConnectTokens = preferencesGson.toJson(connectTokens);
        e.putString(PREFERENCE_KEY_CONNECT_TOKENS, jsonConnectTokens);

        e.apply();
        sCurrentTokens = connectTokens;
    }

    public void updateTokens(final ConnectCallback callback) {
        getConnectApi().refreshAccessTokens("refresh_token", getRefreshToken(),
                ConnectSdk.getClientId(), new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        Validator.validateTokens(connectTokens);
                        storeAndSetTokens(connectTokens);
                        callback.onSuccess(connectTokens);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error != null
                                && error.getResponse() != null
                                && error.getResponse().getStatus() >= 400
                                && error.getResponse().getStatus() < 500) {
                            deleteStoredTokens();
                        }
                        callback.onError(error);
                    }
                });
    }

    private static void deleteStoredTokens() {
        ConnectSdk.getContext()
                .getSharedPreferences(ConnectUtils.PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit().clear().commit();
        sCurrentTokens = null;
    }

    private static ConnectTokens retrieveTokens() {
        if (sCurrentTokens == null) {
            SharedPreferences prefs = ConnectSdk.getContext()
                    .getSharedPreferences(ConnectUtils.PREFERENCES_FILE, Context.MODE_PRIVATE);

            String connectTokensJson = prefs.getString(PREFERENCE_KEY_CONNECT_TOKENS, null);
            ConnectTokens connectTokens = preferencesGson.fromJson(connectTokensJson, ConnectTokens.class);
            sCurrentTokens = connectTokens;
        }
        return sCurrentTokens;
    }
}
