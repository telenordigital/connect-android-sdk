package com.telenor.connect.id;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.utils.ConnectUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.POST;

public class ConnectIdService {

    private interface ConnectAPI {
        @FormUrlEncoded
        @Headers("Content-Type: application/x-www-form-urlencoded")
        @POST("/oauth/token")
        void getAccessTokens(
                @Field("grant_type") String grant_type,
                @Field("code") String code,
                @Field("redirect_uri") String redirect_uri,
                @Field("client_id") String client_id,
                Callback<ConnectTokens> tokens);

        @FormUrlEncoded
        @Headers("Content-Type: application/x-www-form-urlencoded")
        @POST("/oauth/token")
        void refreshAccessTokens(
                @Field("grant_type") String grant_type,
                @Field("refresh_token") String refresh_token,
                @Field("client_id") String client_id,
                Callback<ConnectTokens> tokens);

        @FormUrlEncoded
        @Headers("Content-Type: application/x-www-form-urlencoded")
        @POST("/oauth/revoke")
        void revokeToken(
                @Field("client_id") String client_id,
                @Field("token") String token,
                ResponseCallback callback);
    }

    private static RestAdapter sConnectAdapter;
    private static ConnectAPI sConnectApi;
    private static RequestInterceptor sConnectRetroFitInterceptor;
    private static HttpUrl sConnectUrl;
    private static Gson sGson;
    private static OkHttpClient sHttpClient;

    private static ConnectIdService instance = null;
    private static ConnectTokens sCurrentTokens;

    private ConnectIdService() {
        sConnectUrl = ConnectSdk.getConnectApiUrl();

        sHttpClient = new OkHttpClient();
        sHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        sHttpClient.setReadTimeout(10, TimeUnit.SECONDS);
        sHttpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        sGson = new GsonBuilder().create();

        sConnectRetroFitInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        sConnectAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(sHttpClient))
                .setEndpoint(sConnectUrl.toString())
                .setRequestInterceptor(sConnectRetroFitInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(sGson))
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
        getConnectApi().getAccessTokens("authorization_code", authCode, ConnectSdk.getRedirectUri(),
                ConnectSdk.getClientId(), new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        storeTokens(connectTokens);
                        ConnectUtils.sendTokenStateChanged(true);
                        callback.onSuccess(connectTokens.accessToken);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ConnectUtils.sendTokenStateChanged(false);
                        callback.onError(error.toString());
                    }
                });
    }

    private static ConnectAPI getConnectApi() {
        return sConnectApi;
    }

    private static String getRefreshToken() {
        return retrieveTokens().refreshToken;
    }

    public static void revokeTokens() {
        getConnectApi().revokeToken(ConnectSdk.getClientId(), getAccessToken(), new ResponseCallback() {
            @Override
            public void success(Response response) {}

            @Override
            public void failure(RetrofitError error) {}
        });
        getConnectApi().revokeToken(ConnectSdk.getClientId(), getRefreshToken(), new ResponseCallback() {
            @Override
            public void success(Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
        deleteStoredTokens();
        ConnectUtils.sendTokenStateChanged(false);
    }

    public void startConnectAuthentication(
            Activity activity, ArrayList<String> acrValues, ArrayList<String> scopeTokens) {
        Intent intent = getConnectActivityIntent();
        intent.setAction(ConnectUtils.LOGIN_ACTION);
        if (acrValues != null) {
            intent.putStringArrayListExtra(ConnectUtils.LOGIN_ACR_VALUES, acrValues);
        }
        intent.putStringArrayListExtra(ConnectUtils.LOGIN_SCOPE_TOKENS, scopeTokens);
        activity.startActivityForResult(intent, 1);
    }

    public static void storeTokens(ConnectTokens tokens) {
        SharedPreferences prefs = ConnectSdk.getContext()
                .getSharedPreferences(ConnectUtils.PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        e.putString(ConnectTokens.ACCESS_TOKEN_STRING, tokens.accessToken);
        e.putLong(ConnectTokens.EXPIRES_IN_LONG, tokens.expiresIn);
        e.putString(ConnectTokens.ID_TOKEN_STRING, tokens.idToken);
        e.putString(ConnectTokens.REFRESH_TOKEN_STRING, tokens.refreshToken);
        e.putString(ConnectTokens.SCOPE_STRING, tokens.scope);
        e.putString(ConnectTokens.TOKEN_TYPE_STRING, tokens.tokenType);
        e.apply();
        sCurrentTokens = tokens;
    }

    public void updateTokens() {
        getConnectApi().refreshAccessTokens("refresh_token", getRefreshToken(),
                ConnectSdk.getClientId(), new Callback<ConnectTokens>() {
                    @Override
                    public void success(ConnectTokens connectTokens, Response response) {
                        storeTokens(connectTokens);
                        ConnectUtils.sendTokenStateChanged(true);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse().getStatus() >= 400
                                && error.getResponse().getStatus() < 500) {
                            deleteStoredTokens();
                            ConnectUtils.sendTokenStateChanged(false);
                        }
                    }
                });
    }

    private static void deleteStoredTokens() {
        ConnectSdk.getContext()
                .getSharedPreferences(ConnectUtils.PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit().clear().commit();
        sCurrentTokens = null;
    }

    private Intent getConnectActivityIntent() {
        Intent intent = new Intent();
        intent.setClass(ConnectSdk.getContext(), ConnectActivity.class);

        return intent;
    }

    private static ConnectTokens retrieveTokens() {
        if (sCurrentTokens == null) {
            SharedPreferences prefs = ConnectSdk.getContext()
                    .getSharedPreferences(ConnectUtils.PREFERENCES_FILE, Context.MODE_PRIVATE);
            if (prefs.getString(ConnectTokens.ACCESS_TOKEN_STRING, null) != null) {
                sCurrentTokens = new ConnectTokens(
                        prefs.getString(ConnectTokens.ACCESS_TOKEN_STRING, null),
                        prefs.getLong(ConnectTokens.EXPIRES_IN_LONG, 0),
                        prefs.getString(ConnectTokens.ID_TOKEN_STRING, null),
                        prefs.getString(ConnectTokens.REFRESH_TOKEN_STRING, null),
                        prefs.getString(ConnectTokens.SCOPE_STRING, null),
                        prefs.getString(ConnectTokens.TOKEN_TYPE_STRING, null));
            }
        }
        return sCurrentTokens;
    }
}
