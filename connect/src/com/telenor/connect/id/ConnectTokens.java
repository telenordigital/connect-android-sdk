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
