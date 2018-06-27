package com.telenor.connect.utils;

import android.support.annotation.NonNull;

import com.telenor.connect.id.Claims;

import org.json.JSONException;
import org.json.JSONObject;

public class ClaimsParameterFormatter {

    private static final String USER_INFO = "userinfo";

    /**
     * Following the OpenID Connect 1.0 spec this function encodes a set of Claims as essential,
     * for use as a request parameter.
     * Essential Claim definition: "Claim specified by the Client as being necessary to ensure a
     * smooth authorization experience for the specific task requested by the End-User."
     *
     * In other words, after successfully authorizing the request with the given Claims, the user
     * will have data in the given fields.
     *
     * See http://openid.net/specs/openid-connect-core-1_0.html#ClaimsParameter for more details.
     *
     * @param claims A claims object containing wanted claims.
     * @return A JSON formatted string with all {@code claims} turned into essential claims.
     */
    public static String asJson(Claims claims) {
        final JSONObject essentialTrue = put(new JSONObject(),"essential", true);

        final JSONObject essentials = new JSONObject();
        for (String claim : claims.getClaimsAsSet()) {
            put(essentials, claim, essentialTrue);
        }

        return put(new JSONObject(), USER_INFO, essentials).toString();
    }

    private static JSONObject put(@NonNull JSONObject jsonObject, @NonNull String key, Object value) {
        try {
            return jsonObject.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException("JSONObject unexpectedly threw on `put()`", e);
        }
    }
}
