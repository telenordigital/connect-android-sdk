package com.telenor.connect;

import com.telenor.connect.id.ConnectIdService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PrepareForTest({ConnectIdService.class})
public class ConnectSdkTest {

    @Before
    public void before() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
    }

    @Test
    public void getConnectApiUrlReturnsProductionByDefault() {
        assertThat(ConnectSdk.getConnectApiUrl().toString(),
                is("https://connect.telenordigital.com/"));
    }

    @Test
    public void getsClientIdFromApplicationInfoMetaData() {
        assertThat(ConnectSdk.getClientId(), is("connect-tests"));
    }

    @Test
    public void getsRedirectUriFromApplicationInfoMetaData() {
        assertThat(ConnectSdk.getRedirectUri(), is("connect-tests://oauth2callback"));
    }
}