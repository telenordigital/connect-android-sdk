package com.telenor.connect.utils;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.id.IdToken;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectSdk.class, IdTokenValidator.class})
public class ValidatorTest {

    @Test(expected = NullPointerException.class)
    public void nullArgumentOnNotNullValidationThrows() {
        Validator.notNull(null, "some var");
    }

    @Test
    public void notNullArgumentOnNotNullValidationDoesNothing() {
        Validator.notNull(new Object(), "some var");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNullOrEmptyThrowsOnNull() {
        Validator.notNullOrEmpty(null, "some var");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNullOrEmptyThrowsOnEmpty() {
        Validator.notNullOrEmpty("", "some var");
    }

    @Test(expected = ConnectNotInitializedException.class)
    public void notInitializedSdkThrows() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(false);

        Validator.sdkInitialized();
    }

    public void initializedSdkDoesNotThrow() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);

        Validator.sdkInitialized();
    }

    @Test(expected = ConnectException.class)
    public void validateAuthenticationStateThrowsOnUnEqualState() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.getLastAuthenticationState()).willReturn("some state");

        Validator.validateAuthenticationState("not same state");
    }

    public void validateAuthenticationStateDoesNotThrowOnEqualState() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.getLastAuthenticationState()).willReturn("some state");

        Validator.validateAuthenticationState("some state");
    }

    @Test
    public void validateTokensChecksAllConnectTokensFields() {
        PowerMockito.mockStatic(IdTokenValidator.class);

        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                "access",
                123,
                mock(IdToken.class),
                "refresh",
                "scope",
                "type");

        Validator.validateTokens(connectTokensTO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateTokensMissingAccessTokenThrows() {
        PowerMockito.mockStatic(IdTokenValidator.class);

        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                null,
                123,
                mock(IdToken.class),
                "refresh",
                "scope",
                "type");

        Validator.validateTokens(connectTokensTO);
    }

    public void validateTokensMissingIdTokenIsAllowed() {
        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                "access",
                123,
                null,
                "refresh",
                "scope",
                "type");

        Validator.validateTokens(connectTokensTO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateTokensMissingRefreshTokenThrows() {
        PowerMockito.mockStatic(IdTokenValidator.class);

        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                "access",
                123,
                mock(IdToken.class),
                null,
                "scope",
                "type");

        Validator.validateTokens(connectTokensTO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateTokensMissingScopeThrows() {
        PowerMockito.mockStatic(IdTokenValidator.class);

        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                "access",
                123,
                mock(IdToken.class),
                "refresh",
                null,
                "type");

        Validator.validateTokens(connectTokensTO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateTokensMissingTokenTypeThrows() {
        PowerMockito.mockStatic(IdTokenValidator.class);

        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                "access",
                123,
                mock(IdToken.class),
                "refresh",
                "scope",
                null);

        Validator.validateTokens(connectTokensTO);
    }

    @Test
    public void validStateReturnsTrueWhenCurrentStateIsNull() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.getLastAuthenticationState()).willReturn(null);

        assertThat(Validator.validState("whatever"), is(true));
    }

    @Test
    public void validStateReturnsTrueWhenCurrentStateIsEmpty() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.getLastAuthenticationState()).willReturn("");

        assertThat(Validator.validState("whatever"), is(true));
    }

    @Test
    public void validStateReturnsTrueWhenCurrentStateMatchesGivenState() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.getLastAuthenticationState()).willReturn("abc");

        assertThat(Validator.validState("abc"), is(true));
    }

    @Test
    public void validStateReturnsFalseWhenCurrentStateDoesNotMatchGivenState() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.getLastAuthenticationState()).willReturn("xyz");

        assertThat(Validator.validState("abc"), is(false));
    }
}
