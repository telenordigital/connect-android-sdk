package com.telenor.connect.id;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public final class ConnectTokens implements Serializable {
    @SerializedName("access_token")
    public final String accessToken;

    @SerializedName("expires_in")
    public final long expiresIn;

    @SerializedName("id_token")
    public final IdToken idToken;

    @SerializedName("refresh_token")
    public final String refreshToken;

    public final String scope;

    @SerializedName("token_type")
    public final String tokenType;

    public static final String ACCESS_TOKEN_STRING = "ACCESS_TOKEN_STRING";
    public static final String EXPIRES_IN_LONG = "EXPIRES_IN_LONG";
    public static final String ID_TOKEN_STRING = "ID_TOKEN_STRING";
    public static final String REFRESH_TOKEN_STRING = "REFRESH_TOKEN_STRING";
    public static final String SCOPE_STRING = "SCOPE_STRING";
    public static final String TOKEN_TYPE_STRING = "TOKEN_TYPE_STRING";

    public ConnectTokens(
            String accessToken,
            long expiresIn,
            IdToken idToken,
            String refreshToken,
            String scope,
            String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.tokenType = tokenType;
    }
}
