package com.telenor.connect;

import com.telenor.connect.id.ConnectIdService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectIdService.class})
public class ConnectSdkTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Before
    public void before() {
        Whitebox.setInternalState(ConnectSdk.class, "sSdkInitialized", false);
    }

    @Test
    public void getConnectApiUrlReturnsProductionByDefault() {
        assertThat(ConnectSdk.getConnectApiUrl().toString(),
                is("https://connect.telenordigital.com/"));
    }

    @Test
    public void getsClientIdFromApplicationInfoMetaData() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        assertThat(ConnectSdk.getClientId(), is("connect-tests"));
    }

    @Test
    public void getsRedirectUriFromApplicationInfoMetaData() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        assertThat(ConnectSdk.getRedirectUri(), is("connect-tests://oauth2callback"));
    }

    @Test
    public void getSubjectIdCallsConnectIdService() {
        mockStatic(ConnectIdService.class);

        final String originalSub = "12345";
        ConnectIdService mock = mock(ConnectIdService.class);
        given(ConnectIdService.getInstance()).willReturn(mock);
        when(mock.getSubjectId()).thenReturn(originalSub);

        String sub = ConnectSdk.getSubjectId();
        assertThat(sub, is("12345"));

        verify(mock).getSubjectId();
    }
}