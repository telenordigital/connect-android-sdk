package com.telenor.connect.id;

import com.telenor.connect.ConnectNotSignedInException;
import com.telenor.connect.utils.Validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({Validator.class})
public class ConnectIdServiceTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Test
    public void getAccessTokenRetrievesFromTokenStoreWhenNull() {
        TokenStore tokenStore = mock(TokenStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        final String value = "access token";
        when(connectTokens.getAccessToken()).thenReturn(value);
        when(tokenStore.get()).thenReturn(connectTokens);
        ConnectAPI connectApi = mock(ConnectAPI.class);

        ConnectIdService connectIdService = new ConnectIdService(tokenStore, connectApi, "", "");

        String accessToken = connectIdService.getAccessToken();
        assertThat(accessToken, is(value));
        verify(tokenStore).get();
    }

    @Test
    public void getAccessTokenFetchesFromMemoryWhenAlreadyLoaded() {
        TokenStore tokenStore = mock(TokenStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        final String value = "access token";
        when(connectTokens.getAccessToken()).thenReturn(value);
        when(tokenStore.get()).thenReturn(connectTokens);
        ConnectAPI connectApi = mock(ConnectAPI.class);

        ConnectIdService connectIdService = new ConnectIdService(tokenStore, connectApi, "", "");

        connectIdService.getAccessToken();
        connectIdService.getAccessToken();

        verify(tokenStore, times(1)).get();
    }

    @Test(expected = ConnectNotSignedInException.class)
    public void getUserInfoMissingAccessTokenThrows() {
        TokenStore tokenStore = mock(TokenStore.class);
        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(tokenStore, connectApi, "", "");

        connectIdService.getUserInfo(null);
    }

    @Test
    public void getUserInfoCallsGetUserInfo() {
        TokenStore tokenStore = mock(TokenStore.class);
        ConnectTokens connectTokens = mock(ConnectTokens.class);
        final String value = "access token";
        when(connectTokens.getAccessToken()).thenReturn(value);
        when(tokenStore.get()).thenReturn(connectTokens);

        ConnectAPI connectApi = mock(ConnectAPI.class);
        ConnectIdService connectIdService = new ConnectIdService(tokenStore, connectApi, "", "");

        connectIdService.getUserInfo(null);
        verify(connectApi).getUserInfo("Bearer access token", null);
    }
}
