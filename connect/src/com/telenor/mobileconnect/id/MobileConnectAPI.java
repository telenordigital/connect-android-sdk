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
    @POST("/{token_path}")
    void getAccessTokens(
            @Header("Authorization") String authHeader,
            @Path(value = "token_path", encode = false) String tokenPath,
            @Field("grant_type") String grant_type,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri,
            Callback<ConnectTokensTO> tokens);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/{token_path}")
    void refreshAccessTokens(
            @Header("Authorization") String authHeader,
            @Path(value = "token_path", encode = false) String tokenPath,
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token,
            Callback<ConnectTokensTO> tokens);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/{revoke_path}")
    void revokeToken(
            @Header("Authorization") String authHeader,
            @Path(value = "revoke_path", encode = false) String revokePath,
            @Field("token") String token,
            ResponseCallback callback);

    @Headers("Accept: application/json")
    @GET("/{userinfo_path}")
    void getUserInfo(
            @Header("Authorization") String auth,
            @Path(value = "userinfo_path", encode = false) String userinfo_path,
            Callback<UserInfo> userInfoCallback);
}
