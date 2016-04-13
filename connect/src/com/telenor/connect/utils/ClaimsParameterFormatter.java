package com.telenor.connect.utils;

import com.telenor.connect.id.Claims;

import org.json.JSONException;
import org.json.JSONObject;

public class ClaimsParameterFormatter {

    private static final String USER_INFO = "userinfo";

    public static String asJson(Claims claims) throws JSONException {
        final JSONObject essentialTrue = new JSONObject().put("essential", true);

        final JSONObject essentials = new JSONObject();
        for (String claim : claims.getClaims()) {
            essentials.put(claim, essentialTrue);
        }

        return new JSONObject()
                .put(USER_INFO, essentials)
                .toString();
    }
}