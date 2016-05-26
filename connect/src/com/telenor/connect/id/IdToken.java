package com.telenor.connect.id;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telenor.connect.ConnectException;

import java.text.ParseException;
import java.util.Map;

public class IdToken {

    private final String serializedSignedJwt;
    private final String subject;
    private final String authenticationUsername;
    private final String email;
    private final Boolean emailVerified;
    private final String phoneNumber;
    private final Boolean phoneNumberVerified;

    public IdToken(String serializedSignedJwt) {
        this.serializedSignedJwt = serializedSignedJwt;
        ReadOnlyJWTClaimsSet jwtClaimsSet;

        try {
            jwtClaimsSet = SignedJWT.parse(serializedSignedJwt).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new ConnectException(
                    "Could not parse saved id token. idToken=" + this, e);
        }

        subject = jwtClaimsSet.getSubject();
        final Map<String, Object> cc = jwtClaimsSet.getCustomClaims();


        authenticationUsername = cc.containsKey("td_au") ? (String) cc.get("td_au") : null;
        email = cc.containsKey("email") ? (String) cc.get("email") : null;
        emailVerified
                = cc.containsKey("email_verified") ? (Boolean) cc.get("email_verified") : null;
        phoneNumber = cc.containsKey("phone_number") ? (String) cc.get("phone_number") : null;
        phoneNumberVerified = cc.containsKey("phone_number_verified")
                ? (Boolean) cc.get("phone_number_verified") : null;
    }

    public String getSerializedSignedJwt() {
        return serializedSignedJwt;
    }

    public String getSubject() {
        return subject;
    }

    public String getAuthenticationUsername() {
        return authenticationUsername;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Boolean getPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    @Override
    public String toString() {
        return "IdToken{" +
                "serializedSignedJwt='" + serializedSignedJwt + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
