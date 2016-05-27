package com.telenor.connect.id;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Claims {

    public static final String EMAIL = "email";
    public static final String EMAIL_VERIFIED = "email_verified";
    public static final String NAME = "name";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String PHONE_NUMBER_VERIFIED = "phone_number_verified";
    public static final String SUB = "sub";

    private final Set<String> claims;

    public Claims(String... claims) {
        this.claims = new HashSet<>(Arrays.asList(claims));
    }

    public Set<String> getClaims() {
        return claims;
    }
}