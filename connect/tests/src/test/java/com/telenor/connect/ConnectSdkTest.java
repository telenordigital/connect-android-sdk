package com.telenor.connect;

import android.content.Intent;
import android.net.Uri;

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

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void intentHasValidRedirectUrlCallReturnsTrueOnRedirectLink() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectSdk.getAuthorizeUriAndSetLastAuthState(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("connect-tests://oauth2callback?state=xyz&code=abc"));

        assertThat(ConnectSdk.intentHasValidRedirectUrlCall(intent), is(true));
    }

    @Test
    public void intentHasValidRedirectUrlCallReturnsFalseOnNotRedirectLink() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectSdk.getAuthorizeUriAndSetLastAuthState(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("something-not-registed://oauth2callback?state=xyz&code=abc"));

        assertThat(ConnectSdk.intentHasValidRedirectUrlCall(intent), is(false));
    }

    @Test
    public void intentHasValidRedirectUrlCallReturnsFalseOnWrongState() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectSdk.getAuthorizeUriAndSetLastAuthState(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("something-not-registed://oauth2callback?state=NNN&code=abc"));

        assertThat(ConnectSdk.intentHasValidRedirectUrlCall(intent), is(false));
    }

    @Test
    public void intentHasValidRedirectUrlCallReturnsFalseOnMissingCode() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectSdk.getAuthorizeUriAndSetLastAuthState(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("something-not-registed://oauth2callback?state=xyz"));

        assertThat(ConnectSdk.intentHasValidRedirectUrlCall(intent), is(false));
    }
}