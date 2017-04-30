package com.telenor.mobileconnect.id;

import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.id.UserInfo;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

public interface MobileConnectAPI {

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/{operator_prefix}/token")
    void getAccessTokens(
            @Header("Authorization") String authHeader,
            @Path("operator_prefix") String operatorPrefix,
            @Field("grant_type") String grant_type,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri,
            Callback<ConnectTokensTO> tokens);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/{operator_prefix}/token")
    void refreshAccessTokens(
            @Header("Authorization") String authHeader,
            @Path("operator_prefix") String operatorPrefix,
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token,
            Callback<ConnectTokensTO> tokens);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/{operator_prefix}/revoke")
    void revokeToken(
            @Header("Authorization") String authHeader,
            @Path("operator_prefix") String operatorPrefix,
            @Field("token") String token,
            ResponseCallback callback);

    @Headers("Accept: application/json")
    @GET("/oauth/{operator_prefix}/userinfo")
    void getUserInfo(
            @Header("Authorization") String auth,
            @Path("operator_prefix") String operator_prefix,
            Callback<UserInfo> userInfoCallback);
}
