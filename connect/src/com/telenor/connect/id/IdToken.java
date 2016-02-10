package com.telenor.connect.id;

public class IdToken {
    private final String serializedSignedJwt;

    public IdToken(String serializedSignedJwt) {
        this.serializedSignedJwt = serializedSignedJwt;
    }

    public String getSerializedSignedJwt() {
        return serializedSignedJwt;
    }
}
