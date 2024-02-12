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
    @POST("/connect/token")
    Call<ConnectTokensTO> getAccessTokenFromCode(
            @Field("grant_type") String grant_type,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri,
            @Field("client_id") String client_id,
            @Field("code_verifier") String code_verifier,
            @Field("scope") String scope
    );

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/connect/token")
    Call<ConnectTokensTO> refreshAccessTokens(
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token,
            @Field("client_id") String client_id);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/token")
    Call<ConnectTokensTO> refreshLegacyAccessTokens(
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token,
            @Field("client_id") String client_id);

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/v1/logout")
    Call<Void> logOut(@Field("id_token_hint") String id_token_hint);
}
