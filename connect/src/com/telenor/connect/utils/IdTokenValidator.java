package com.telenor.connect.utils;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.IdToken;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdTokenValidator {

    public static void validate(final IdToken idToken) {
        final ReadOnlyJWTClaimsSet idTokenClaimsSet;

        try {
            final SignedJWT signedJwt = SignedJWT.parse(idToken.getSerializedSignedJwt());
            idTokenClaimsSet = signedJwt.getJWTClaimsSet();
        } catch (final ParseException e) {
            throw new ConnectException(
                    "Failed to parse ID token. serializedIdToken="
                            + idToken.getSerializedSignedJwt(), e);
        }

        final String iss = idTokenClaimsSet.getIssuer();
        final String expectedIssuer
                = ConnectSdk.getConnectApiUrl().toString() + ConnectSdk.OAUTH_PATH;
        if (!expectedIssuer.equals(iss)) {
            throw new ConnectException(
                    "ID token issuer is not the same as the issuer this client is configured with."
                            + " expectedIssuer=" + expectedIssuer
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        final List<String> audience = idTokenClaimsSet.getAudience();
        final String clientId = ConnectSdk.getClientId();
        if (audience == null || !audience.contains(clientId)) {
            throw new ConnectException(
                    "ID token audience list does not contain the configured client ID."
                            + " clientId=" + clientId
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        final Set<String> untrustedAudiences = new HashSet<>(idTokenClaimsSet.getAudience());
        untrustedAudiences.remove(clientId);
        if (untrustedAudiences.size() != 0) {
            throw new ConnectException(
                    "ID token audience list contains untrusted audiences."
                            + " untrustedAudiences=" + untrustedAudiences
                            + " trustedAudiences=" + clientId
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        final String authorizedParty = (String) idTokenClaimsSet.getCustomClaim("azp");
        if (idTokenClaimsSet.getAudience().size() > 1 && authorizedParty == null) {
            throw new ConnectException(
                    "ID token contains multiple audiences but no azp claim is present."
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        if (authorizedParty != null && !clientId.equals(authorizedParty)) {
            throw new ConnectException(
                    "ID token authorized party is not the configured client ID."
                            + " configuredClientId=" + clientId
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        if (idTokenClaimsSet.getExpirationTime() == null
                || idTokenClaimsSet.getExpirationTime().before(new Date())) {
            throw new ConnectException("ID token has expired."
                    + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        if (idTokenClaimsSet.getIssueTime() == null) {
            throw new ConnectException("ID token is missing the \"iat\" claim."
                    + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }
    }
}
