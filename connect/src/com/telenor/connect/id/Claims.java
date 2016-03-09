package com.telenor.connect.id;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Claims {

    public static final String OPENID = "openid";
    public static final String PROFILE = "profile";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";
    public static final String PHONE = "phone";

    private final Set<String> claims;

    public Claims(String... claims) {
        this.claims = new HashSet<>(Arrays.asList(claims));
    }

    public Set<String> getClaims() {
        return claims;
    }
}