package com.telenor.connect.id;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telenor.connect.ConnectException;

import java.io.Serializable;
import java.text.ParseException;

public class IdToken implements Serializable {

    private final String serializedSignedJwt;
    private final String subject;

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
    }

    public String getSerializedSignedJwt() {
        return serializedSignedJwt;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "IdToken{" +
                "serializedSignedJwt='" + serializedSignedJwt + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
