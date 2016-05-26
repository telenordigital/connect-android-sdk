package com.telenor.connect.id;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telenor.connect.ConnectException;

import java.text.ParseException;
import java.util.Map;

public class IdToken {

    private static final String AUTHENTICATED_USERNAME = "td_au";
    private static final String EMAIL = "email";
    private static final String EMAIL_VERIFIED = "email_verified";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String PHONE_NUMBER_VERIFIED = "phone_number_verified";

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

        final Map<String, Object> cc = jwtClaimsSet.getCustomClaims();
        subject = jwtClaimsSet.getSubject();
        authenticationUsername = cc.containsKey(AUTHENTICATED_USERNAME)
                ? (String) cc.get(AUTHENTICATED_USERNAME) : null;
        email = cc.containsKey(EMAIL) ? (String) cc.get(EMAIL) : null;
        emailVerified
                = cc.containsKey(EMAIL_VERIFIED) ? (Boolean) cc.get(EMAIL_VERIFIED) : null;
        phoneNumber = cc.containsKey(PHONE_NUMBER) ? (String) cc.get(PHONE_NUMBER) : null;
        phoneNumberVerified = cc.containsKey(PHONE_NUMBER_VERIFIED)
                ? (Boolean) cc.get(PHONE_NUMBER_VERIFIED) : null;
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
                ", authenticationUsername='" + authenticationUsername + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", phoneNumberVerified=" + phoneNumberVerified +
                '}';
    }
}
