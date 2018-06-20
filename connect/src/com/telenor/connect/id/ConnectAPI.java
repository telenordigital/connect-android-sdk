package com.telenor.connect.id;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ConnectAPI {

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/token")
    Call<ConnectTokensTO> getAccessTokens(
            @Field("grant_type") String grant_type,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri,
            @Field("client_id") String client_id);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/token")
    Call<ConnectTokensTO> refreshAccessTokens(
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token,
            @Field("client_id") String client_id);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/revoke")
    Call<Void> revokeToken(
            @Field("client_id") String client_id,
            @Field("token") String token);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/logout")
    Call<Void> logOut(@Header("Authorization") String auth);

    @Headers("Accept: application/json")
    @GET("/oauth/userinfo")
    Call<UserInfo> getUserInfo(@Header("Authorization") String auth);
}
