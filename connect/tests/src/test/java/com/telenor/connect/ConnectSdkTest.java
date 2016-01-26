package com.telenor.connect;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class ConnectSdkTest {
    @Before
    public void before() {
        Whitebox.setInternalState(ConnectSdk.class, "sSdkInitialized", false);
    }

    @Test
    public void testProductionByDefault() {
        assertEquals("https://connect.telenordigital.com/",
                ConnectSdk.getConnectApiUrl().toString());
    }

    @Test
    public void testGetClientId() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        assertEquals("connect-tests", ConnectSdk.getClientId());
    }

    @Test
    public void testGetRedirectUri() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        assertEquals("connect-tests://oauth2callback", ConnectSdk.getRedirectUri());
    }

}
