package com.telenor.connect.id;

import java.io.Serializable;

public class IdToken implements Serializable {

    private final String serializedSignedJwt;

    public IdToken(String serializedSignedJwt) {
        this.serializedSignedJwt = serializedSignedJwt;
    }

    public String getSerializedSignedJwt() {
        return serializedSignedJwt;
    }
}
