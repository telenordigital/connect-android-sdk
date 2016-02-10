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

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectSdk.class)
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


        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setIssuer("https://connect.telenordigital.com/oauth");
        claimsSet.setAudience("connect-tests");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1); // this bit is changed from normal
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

}
