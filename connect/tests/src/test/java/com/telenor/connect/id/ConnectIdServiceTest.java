package com.telenor.connect.id;

import com.telenor.connect.ConnectNotSignedInException;
import com.telenor.connect.utils.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;

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
@Config(sdk = 18)
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
        Call call = mock(Call.class);
        when(connectApi.getUserInfo(anyString())).thenReturn(call);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getUserInfo(null);
        verify(connectApi).getUserInfo("Bearer access token");
    }

    @Test
    public void getValidAccessTokenCallsNoSignedInUserWhenTokensAreMissing() {
        ConnectStore connectStore = mock(ConnectStore.class);
        when(connectStore.get()).thenReturn(null);

        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");
        connectIdService.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void success(String accessToken) {
                fail();
            }

            @Override
            public void unsuccessfulResult(Response response, boolean userDataRemoved) {
                fail();
            }

            @Override
            public void failure(Call<ConnectTokensTO> call, Throwable error) {
                fail();
            }

            @Override
            public void noSignedInUser() {
                return; // success
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
        Call call = mock(Call.class);
        when(connectApi.refreshAccessTokens(anyString(), anyString(), anyString()))
                .thenReturn(call);
        ConnectIdService connectIdService = new ConnectIdService(connectStore, connectApi, "", "");

        connectIdService.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void success(String accessToken) {}

            @Override
            public void unsuccessfulResult(Response response, boolean userDataRemoved) {}

            @Override
            public void failure(Call<ConnectTokensTO> call, Throwable error) {}

            @Override
            public void noSignedInUser() {}
        });

        verify(connectApi).refreshAccessTokens(
                anyString(),
                eq("refresh_token"),
                anyString());
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
            public void success(String accessToken) {
                assertThat(accessToken, is("access_token"));
            }

            @Override
            public void unsuccessfulResult(Response response, boolean userDataRemoved) {
                fail();
            }

            @Override
            public void failure(Call<ConnectTokensTO> call, Throwable error) {
                fail();
            }

            @Override
            public void noSignedInUser() {
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
