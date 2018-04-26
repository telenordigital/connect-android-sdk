package com.telenor.connect.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectTokensTO;
import com.telenor.connect.id.IdToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@Config(sdk = 18)
@PrepareForTest({ConnectSdk.class, IdTokenValidator.class, TextUtils.class, RestHelper.class})
public class ValidatorTest {

    @Before
    public void mockConnectSdk() {
        Context context = mock(Context.class);
        SharedPreferences sharedPrefs = mock(SharedPreferences.class);
        PowerMockito.mockStatic(TextUtils.class);
        when(sharedPrefs.getString(anyString(), anyString())).thenReturn(null);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);

        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
    }

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
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(false);

        Validator.sdkInitialized();
    }

    public void initializedSdkDoesNotThrow() {
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);

        Validator.sdkInitialized();
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

        Validator.validateTokens(connectTokensTO, null);
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

        Validator.validateTokens(connectTokensTO, null);
    }

    public void validateTokensMissingIdTokenIsAllowed() {
        ConnectTokensTO connectTokensTO = new ConnectTokensTO(
                "access",
                123,
                null,
                "refresh",
                "scope",
                "type");

        Validator.validateTokens(connectTokensTO, null);
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

        Validator.validateTokens(connectTokensTO, null);
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

        Validator.validateTokens(connectTokensTO, null);
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

        Validator.validateTokens(connectTokensTO, null);
    }
}
