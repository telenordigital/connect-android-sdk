package com.telenor.connect.id;

import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;

public interface ConnectAPI {

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/token")
    void getAccessTokens(
            @Field("grant_type") String grant_type,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri,
            @Field("client_id") String client_id,
            Callback<ConnectTokensTO> tokens);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/token")
    void refreshAccessTokens(
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token,
            @Field("client_id") String client_id,
            Callback<ConnectTokensTO> tokens);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/revoke")
    void revokeToken(
            @Field("client_id") String client_id,
            @Field("token") String token,
            ResponseCallback callback);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/logout")
    void logOut(@Header("Authorization") String auth, ResponseCallback callback);

    @Headers("Accept: application/json")
    @GET("/oauth/userinfo")
    void getUserInfo(@Header("Authorization") String auth, Callback<UserInfo> userInfoCallback);

    @Headers("Accept: application/json")
    @GET("/id/api/get-header-enrichment-token")
    void getHeaderEnrichmentToken(Callback<JsonObject> callback);
}
