package com.telenor.connect.id;

import com.google.gson.annotations.SerializedName;

public class ConnectTokens {
    @SerializedName("access_token")
    private final String accessToken;

    @SerializedName("expires_in")
    private final long expiresIn;

    @SerializedName("id_token")
    private final IdToken idToken;

    @SerializedName("refresh_token")
    private final String refreshToken;

    private final String scope;

    @SerializedName("token_type")
    private final String tokenType;

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

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public IdToken getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return tokenType;
    }
}
