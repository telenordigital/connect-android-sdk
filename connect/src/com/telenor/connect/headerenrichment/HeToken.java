package com.telenor.connect.headerenrichment;

import java.util.Date;

public class HeToken {
    private final String token;
    private final Date expiration;
    private final String gifUrl;

    public HeToken(String token, Date expiration, String gifUrl) {
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
