package com.telenor.connect.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.IdToken;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import okhttp3.HttpUrl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@Config(sdk = 18)
@PowerMockIgnore("javax.crypto.*")
@PrepareForTest({ConnectSdk.class, ConnectUrlHelper.class})
public class IdTokenValidatorTest {

    private static IdToken normalSerializedSignedJwt;
    private static Date oneHourIntoFuture;
    private static Date now;
    private static Date tenYearsIntoFuture;
    private static Date twoHoursAgo;

    private static final String SHARED_SECRET_TEST_SIGNATURE = "secret01234567890ABCDEFGHIJKLMNO";

    @BeforeClass
    public static void setUp() throws Exception {
        Calendar calendar = Calendar.getInstance();
        now = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        oneHourIntoFuture = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, 10);
        tenYearsIntoFuture = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, -2);
        twoHoursAgo = calendar.getTime();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://connect.telenordigital.com/oauth")
                .audience("connect-tests")
                .expirationTime(oneHourIntoFuture)
                .issueTime(now)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        JWSSigner signer = new MACSigner(SHARED_SECRET_TEST_SIGNATURE);
        signedJWT.sign(signer);
        normalSerializedSignedJwt = new IdToken(signedJWT.serialize());
    }

    @Before
    public void beforeEach() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
        PowerMockito.mockStatic(ConnectUrlHelper.class);
    }

    @Test(expected = ConnectException.class)
    public void brokenJwtThrowsConnectException() {
        IdTokenValidator.validate(new IdToken("<not correct>"), null);
    }

    @Test
    public void correctIdTokenDoesNotThrow() {
        BDDMockito.given(ConnectUrlHelper.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");
        BDDMockito.given(ConnectSdk.getExpectedIssuer())
                .willReturn("https://connect.telenordigital.com/oauth");
        BDDMockito.given(ConnectSdk.getExpectedAudiences())
                .willReturn(Collections.singletonList("connect-tests"));

        IdTokenValidator.validate(normalSerializedSignedJwt, null);
    }

    @Test(expected = ConnectException.class)
    public void unequalIssuerThrows() {
        BDDMockito.given(ConnectUrlHelper.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com.fishyou.biz"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");
        BDDMockito.given(ConnectSdk.getExpectedIssuer())
                .willReturn("https://connect.telenordigital.com/oauth");

        IdTokenValidator.validate(normalSerializedSignedJwt, null);
    }

    @Test(expected = ConnectException.class)
    public void mismatchingClientIdThrows() {
        BDDMockito.given(ConnectUrlHelper.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("something-else");
        BDDMockito.given(ConnectSdk.getExpectedIssuer())
                .willReturn("https://connect.telenordigital.com/oauth");

        IdTokenValidator.validate(normalSerializedSignedJwt, null);
    }

    @Test(expected = ConnectException.class)
    public void authorizedPartyNotEqualClientThrows() throws Exception {
        BDDMockito.given(ConnectUrlHelper.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");
        BDDMockito.given(ConnectSdk.getExpectedIssuer())
                .willReturn("https://connect.telenordigital.com/oauth");

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://connect.telenordigital.com/oauth")
                .audience("connect-tests")
                .expirationTime(oneHourIntoFuture)
                .issueTime(now)
                .claim("azp", "NOT connect-tests")
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        JWSSigner signer = new MACSigner(SHARED_SECRET_TEST_SIGNATURE);
        signedJWT.sign(signer);
        IdToken idToken = new IdToken(signedJWT.serialize());


        IdTokenValidator.validate(idToken, null);
    }

    @Test(expected = ConnectException.class)
    public void expiredTimeThrows() throws Exception {
        BDDMockito.given(ConnectUrlHelper.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");
        BDDMockito.given(ConnectSdk.getExpectedIssuer())
                .willReturn("https://connect.telenordigital.com/oauth");

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://connect.telenordigital.com/oauth")
                .audience("connect-tests")
                .expirationTime(twoHoursAgo)
                .issueTime(now)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        JWSSigner signer = new MACSigner(SHARED_SECRET_TEST_SIGNATURE);
        signedJWT.sign(signer);
        IdToken idToken = new IdToken(signedJWT.serialize());

        IdTokenValidator.validate(idToken, null);
    }

    @Test(expected = ConnectException.class)
    public void missingIssueTimeThrows() throws Exception {
        BDDMockito.given(ConnectUrlHelper.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");
        BDDMockito.given(ConnectSdk.getExpectedIssuer())
                .willReturn("https://connect.telenordigital.com/oauth");

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://connect.telenordigital.com/oauth")
                .audience("connect-tests")
                .expirationTime(oneHourIntoFuture)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        JWSSigner signer = new MACSigner(SHARED_SECRET_TEST_SIGNATURE);
        signedJWT.sign(signer);
        IdToken idToken = new IdToken(signedJWT.serialize());

        IdTokenValidator.validate(idToken, null);
    }

    @Test
    public void isValidExpirationTimeReturnsFalseOnNullExpDate() {
        assertThat(IdTokenValidator.isValidExpirationTime(null, now, null), is(false));
    }

    @Test
    public void isValidExpirationTimeReturnsTrueWhenExpDateIsAfterCurrentDate() {
        assertThat(IdTokenValidator.isValidExpirationTime(oneHourIntoFuture, now, null), is(true));
    }

    @Test
    public void isValidExpirationTimeReturnsFalseWhenCurrentTimeIsAfterExpAndServerTimestampIsMissing() {
        assertThat(IdTokenValidator.isValidExpirationTime(now, oneHourIntoFuture, null), is(false));
    }

    @Test
    public void isValidExpirationTimeReturnsTrueWhenCurrentTimeIsAfterExpButServerTimestampIsBefore() {
        assertThat(
                IdTokenValidator.isValidExpirationTime(oneHourIntoFuture, tenYearsIntoFuture, now),
                is(true));
    }

    @Test
    public void isValidExpirationTimeReturnsFalseWhenTokenActuallyIsExpired() {
        assertThat(
                IdTokenValidator.isValidExpirationTime(twoHoursAgo, tenYearsIntoFuture, now),
                is(false));
    }

}
