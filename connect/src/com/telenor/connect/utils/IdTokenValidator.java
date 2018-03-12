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

    public static void validate(final IdToken idToken, Date serverTimestamp) {
        final ReadOnlyJWTClaimsSet idTokenClaimsSet;

        try {
            final SignedJWT signedJwt = SignedJWT.parse(idToken.getSerializedSignedJwt());
            idTokenClaimsSet = signedJwt.getJWTClaimsSet();
        } catch (final ParseException e) {
            throw new ConnectException(
                    "Failed to parse ID token. serializedIdToken="
                            + idToken.getSerializedSignedJwt(), e);
        }

        final String issuer = idTokenClaimsSet.getIssuer();
        final String expectedIssuer = ConnectSdk.getExpectedIssuer();
        if (!expectedIssuer.equals(issuer)) {
            throw new ConnectException(
                    "ID token issuer is not the same as the issuer this client is configured with."
                            + " expectedIssuer=" + expectedIssuer
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        final String clientId = ConnectSdk.getClientId();
        final List<String> audience = idTokenClaimsSet.getAudience();
        final List<String> expectedAudience = ConnectSdk.getExpectedAudiences();
        if (audience == null || !audience.containsAll(expectedAudience)) {
            throw new ConnectException(
                    "ID token audience list does not contain the configured client ID."
                            + " clientId=" + clientId
                            + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }

        final Set<String> untrustedAudiences = new HashSet<>(idTokenClaimsSet.getAudience());
        untrustedAudiences.removeAll(expectedAudience);
        if (untrustedAudiences.size() != 0) {
            throw new ConnectException(
                    "ID token audience list contains untrusted audiences."
                            + " untrustedAudiences=" + untrustedAudiences
                            + " trustedAudiences=" + expectedAudience
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

        final Date expirationTime = idTokenClaimsSet.getExpirationTime();
        if (!isValidExpirationTime(expirationTime, new Date(), serverTimestamp)) {
            throw new ConnectException("ID token has expired."
                    + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }


        if (idTokenClaimsSet.getIssueTime() == null) {
            throw new ConnectException("ID token is missing the \"iat\" claim."
                    + " idTokenClaimsSet=" + idTokenClaimsSet.toJSONObject());
        }
    }

    public static boolean isValidExpirationTime(
            Date expirationTime, Date currentDate, Date serverTimestamp) {
        if (expirationTime == null) {
            return false;
        }

        if (expirationTime.after(currentDate)) {
            return true;
        }

        if (serverTimestamp == null) {
            return false;
        }

        return expirationTime.after(serverTimestamp);
    }
}
