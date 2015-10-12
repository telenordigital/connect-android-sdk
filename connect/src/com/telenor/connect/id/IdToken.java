package com.telenor.connect.id;

public class IdToken {
    private final String sIdToken;

    public IdToken(String idToken) {
        sIdToken = idToken;
    }

    @Override
    public String toString() {
        return sIdToken;
    }
}
