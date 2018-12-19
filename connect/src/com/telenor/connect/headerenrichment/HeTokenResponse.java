package com.telenor.connect.headerenrichment;

import java.util.Date;

public class HeTokenResponse {
    private final String token;
    private final Date expiration;
    private final String gifUrl;

    public HeTokenResponse(String token, Date expiration, String gifUrl) {
        this.token = token;
        this.expiration = expiration;
        this.gifUrl = gifUrl;
    }

    public String getToken() {
        return token;
    }

    public Date getExpiration() {
        return expiration;
    }

    public String getGifUrl() {
        return gifUrl;
    }
}
