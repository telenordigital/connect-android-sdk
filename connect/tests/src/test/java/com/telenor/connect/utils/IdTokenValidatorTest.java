package com.telenor.connect.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.IdToken;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectSdk.class, InternetTime.class})
public class IdTokenValidatorTest {

    private static IdToken normalSerializedSignedJwt;

    @BeforeClass
    public static void setUp() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setIssuer("https://connect.telenordigital.com/oauth");
        claimsSet.setAudience("connect-tests");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        claimsSet.setExpirationTime(calendar.getTime());
        claimsSet.setIssueTime(new Date());

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.ES256), claimsSet);
        signedJWT.sign(new ECDSASigner(new BigInteger("123")));
        normalSerializedSignedJwt = new IdToken(signedJWT.serialize());
    }

    @Before
    public void beforeEach() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
    }

    @Test(expected = ConnectException.class)
    public void brokenJwtThrowsConnectException() {
        IdTokenValidator.validate(new IdToken("<not correct>"));
    }

    @Test
    public void correctIdTokenDoesNotThrow() {
        BDDMockito.given(ConnectSdk.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");

        IdTokenValidator.validate(normalSerializedSignedJwt);
    }

    @Test(expected = ConnectException.class)
    public void unequalIssuerThrows() {
        BDDMockito.given(ConnectSdk.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com.fishyou.biz"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");

        IdTokenValidator.validate(normalSerializedSignedJwt);
    }

    @Test(expected = ConnectException.class)
    public void mismatchingClientIdThrows() {
        BDDMockito.given(ConnectSdk.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("something-else");

        IdTokenValidator.validate(normalSerializedSignedJwt);
    }

    @Test(expected = ConnectException.class)
    public void authorizedPartyNotEqualClientThrows() throws Exception {
        BDDMockito.given(ConnectSdk.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");


        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setIssuer("https://connect.telenordigital.com/oauth");
        claimsSet.setAudience("connect-tests");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        claimsSet.setExpirationTime(calendar.getTime());
        claimsSet.setIssueTime(new Date());
        claimsSet.setCustomClaim("azp", "NOT connect-tests");

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.ES256), claimsSet);
        signedJWT.sign(new ECDSASigner(new BigInteger("123")));
        IdToken idToken = new IdToken(signedJWT.serialize());


        IdTokenValidator.validate(idToken);
    }

    @Test(expected = ConnectException.class)
    public void expiredTimeThrows() throws Exception {
        BDDMockito.given(ConnectSdk.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");
        PowerMockito.mockStatic(InternetTime.class);
        BDDMockito.given(InternetTime.getInternetDate()).willReturn(new Date());


        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setIssuer("https://connect.telenordigital.com/oauth");
        claimsSet.setAudience("connect-tests");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -2); // this bit is changed from normal
        claimsSet.setExpirationTime(calendar.getTime());
        claimsSet.setIssueTime(new Date());

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.ES256), claimsSet);
        signedJWT.sign(new ECDSASigner(new BigInteger("123")));
        IdToken idToken = new IdToken(signedJWT.serialize());

        IdTokenValidator.validate(idToken);
    }

    @Test(expected = ConnectException.class)
    public void missingIssueTimeThrows() throws Exception {
        BDDMockito.given(ConnectSdk.getConnectApiUrl())
                .willReturn(HttpUrl.parse("https://connect.telenordigital.com"));
        BDDMockito.given(ConnectSdk.getClientId()).willReturn("connect-tests");


        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setIssuer("https://connect.telenordigital.com/oauth");
        claimsSet.setAudience("connect-tests");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        claimsSet.setExpirationTime(calendar.getTime());

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.ES256), claimsSet);
        signedJWT.sign(new ECDSASigner(new BigInteger("123")));
        IdToken idToken = new IdToken(signedJWT.serialize());

        IdTokenValidator.validate(idToken);
    }

    @Test
    public void isValidExpirationTimeReturnsFalseOnNullDate() {
        boolean actual = IdTokenValidator.isValidExpirationTime(null, new Date());
        assertThat(actual, is(false));
    }

    @Test
    public void isValidExpirationTimeChecksInternetDateOnExpiredToken() throws Exception {
        PowerMockito.mockStatic(InternetTime.class);
        BDDMockito.given(InternetTime.getInternetDate()).willReturn(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -2);
        boolean actual = IdTokenValidator.isValidExpirationTime(calendar.getTime(), new Date());
        assertThat(actual, is(false));

        PowerMockito.verifyStatic();
        InternetTime.getInternetDate();
    }

    @Test
    public void isValidExpirationTimeDoesNotCheckInternetDateOnValidToken() throws Exception {
        PowerMockito.mockStatic(InternetTime.class);
        BDDMockito.given(InternetTime.getInternetDate()).willReturn(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 2);
        boolean actual = IdTokenValidator.isValidExpirationTime(calendar.getTime(), new Date());
        assertThat(actual, is(true));

        PowerMockito.verifyStatic(BDDMockito.never());
        InternetTime.getInternetDate();
    }

    @Test
    public void isValidExpirationTimeReturnsFalseOnIOExceptionWhenCheckingInternetTime()
            throws Exception {
        PowerMockito.mockStatic(InternetTime.class);
        BDDMockito.given(InternetTime.getInternetDate()).willThrow(new IOException());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -2);
        boolean actual = IdTokenValidator.isValidExpirationTime(calendar.getTime(), new Date());
        assertThat(actual, is(false));

        PowerMockito.verifyStatic();
        InternetTime.getInternetDate();
    }

    @Test public void isValidExpirationTimeReturnsTrueOnIncorrectSystemTimeButValidExpirationTime()
            throws Exception {
        PowerMockito.mockStatic(InternetTime.class);
        Date actualCurrentTime = new Date();
        BDDMockito.given(InternetTime.getInternetDate()).willReturn(actualCurrentTime);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 2);
        Date expirationTime = calendar.getTime();
        calendar.add(Calendar.HOUR, 100);
        Date futureTime = calendar.getTime();
        boolean actual = IdTokenValidator.isValidExpirationTime(expirationTime, futureTime);
        assertThat(actual, is(true));

        PowerMockito.verifyStatic();
        InternetTime.getInternetDate();
    }

}
