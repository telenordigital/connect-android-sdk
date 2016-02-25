package com.telenor.connect.id;

import android.content.Context;
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

    private static final String PREFERENCES_FILE = "com.telenor.connect.PREFERENCES_FILE";
    private static final String PREFERENCE_KEY_CONNECT_TOKENS = "CONNECT_TOKENS";
    private static final Gson preferencesGson = new Gson();

    private static ConnectIdService sInstance = null;

    private final ConnectAPI connectApi;

    private ConnectTokens currentTokens;

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
                        storeTokens(connectTokens);
                        currentTokens = connectTokens;
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

    private void storeTokens(ConnectTokens connectTokens) {
        String jsonConnectTokens = preferencesGson.toJson(connectTokens);

        ConnectSdk.getContext()
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFERENCE_KEY_CONNECT_TOKENS, jsonConnectTokens)
                .apply();
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
        deleteStoredTokens();
        currentTokens = null;
        ConnectUtils.sendTokenStateChanged(false);
    }

    private void deleteStoredTokens() {
        ConnectSdk.getContext()
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    public void updateTokens(final ConnectCallback callback) {
        connectApi.refreshAccessTokens("refresh_token", getRefreshToken(),
                ConnectSdk.getClientId(), new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        Validator.validateTokens(connectTokens);
                        storeTokens(connectTokens);
                        currentTokens = connectTokens;
                        callback.onSuccess(connectTokens);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error != null
                                && error.getResponse() != null
                                && error.getResponse().getStatus() >= 400
                                && error.getResponse().getStatus() < 500) {
                            deleteStoredTokens();
                            currentTokens = null;
                        }
                        callback.onError(error);
                    }
                });
    }

    private ConnectTokens retrieveTokens() {
        if (currentTokens == null) {
            currentTokens = getTokensFromStore();
        }
        return currentTokens;
    }

    private ConnectTokens getTokensFromStore() {
        String connectTokensJson
                = ConnectSdk.getContext()
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getString(PREFERENCE_KEY_CONNECT_TOKENS, null);

        return preferencesGson.fromJson(connectTokensJson, ConnectTokens.class);
    }
}
