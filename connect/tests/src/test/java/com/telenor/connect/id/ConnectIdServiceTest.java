package com.telenor.connect.id;

import com.telenor.connect.ConnectNotSignedInException;
import com.telenor.connect.ConnectRefreshTokenMissingException;
import com.telenor.connect.utils.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import retrofit.Callback;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PrepareForTest({Validator.class})
public class ConnectIdServiceTest {

    @Test
    public void getAccessTokenRetrievesFromTokenStoreWhenNull() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        final String value = "access token";
        when(connectTokens.getAccessToken()).thenReturn(value);
        when(connectStore.get()).thenReturn(connectTokens);
        ConnectAPI connectApi = mock(ConnectAPI.class);

        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        String accessToken = connectIdService.getAccessToken();
        assertThat(accessToken, is(value));
        verify(connectStore).get();
    }

    @Test
    public void getAccessTokenFetchesFromMemoryWhenAlreadyLoaded() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        final String value = "access token";
        when(connectTokens.getAccessToken()).thenReturn(value);
        when(connectStore.get()).thenReturn(connectTokens);
        ConnectAPI connectApi = mock(ConnectAPI.class);

        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getAccessToken();
        connectIdService.getAccessToken();

        verify(connectStore, times(1)).get();
    }

    @Test(expected = ConnectNotSignedInException.class)
    public void getUserInfoMissingAccessTokenThrows() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getUserInfo(null);
    }

    @Test
    public void getUserInfoCallsGetUserInfo() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        final String value = "access token";
        when(connectTokens.getAccessToken()).thenReturn(value);
        when(connectStore.get()).thenReturn(connectTokens);

        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getUserInfo(null);
        verify(connectApi).getUserInfo("Bearer access token", null);
    }

    @Test(expected = ConnectRefreshTokenMissingException.class)
    public void getValidAccessTokenThrowsWhenTokensAreMissing() {
        ConnectStore connectStore = mock(ConnectStore.class);
        when(connectStore.get()).thenReturn(null);

        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
            }
            @Override
            public void onError(Object errorData) {
            }
        });
    }

    @Test
    public void getValidAccessTokenCallsUpdateTokensWhenAccessTokenIsExpired() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        when(connectTokens.accessTokenHasExpired()).thenReturn(true);
        when(connectTokens.getRefreshToken()).thenReturn("refresh_token");
        when(connectStore.get()).thenReturn(connectTokens);

        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
            }
            @Override
            public void onError(Object errorData) {
            }
        });

        verify(connectApi).refreshAccessTokens(
                anyString(),
                eq("refresh_token"),
                anyString(),
                Matchers.<Callback<ConnectTokensTO>>any());
    }

    @Test
    public void getValidAccessTokenCallsCallbackWhenAccessTokenIsntExpired() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        when(connectTokens.accessTokenHasExpired()).thenReturn(false);
        when(connectTokens.getAccessToken()).thenReturn("access_token");
        when(connectStore.get()).thenReturn(connectTokens);

        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                assertThat(accessToken, is("access_token"));
            }
            @Override
            public void onError(Object errorData) {
                fail();
            }
        });
    }

    @Test
    public void getAccessTokenExpirationTimeRetrievesFromTokenStoreWhenNull() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        Date value = new Date();
        when(connectTokens.getExpirationDate()).thenReturn(value);
        when(connectStore.get()).thenReturn(connectTokens);
        ConnectAPI connectApi = mock(ConnectAPI.class);

        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        Date date = connectIdService.getAccessTokenExpirationTime();
        assertThat(date, is(value));
        verify(connectStore).get();
    }

    @Test
    public void getAccessTokenExpirationTimeFetchesFromMemoryWhenAlreadyLoaded() {
        ConnectStore connectStore = mock(ConnectStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        Date value = new Date();
        when(connectTokens.getExpirationDate()).thenReturn(value);
        when(connectStore.get()).thenReturn(connectTokens);
        ConnectAPI connectApi = mock(ConnectAPI.class);

        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getAccessTokenExpirationTime();
        connectIdService.getAccessTokenExpirationTime();

        verify(connectStore, times(1)).get();
    }
}
