package com.telenor.connect.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectSdk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectSdk.class, LocalBroadcastManager.class})
public class ConnectUtilsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Test
    public void sendTokenStateChangedBroadcastsIntent() {
        PowerMockito.mockStatic(LocalBroadcastManager.class);

        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
        Context context = mock(Context.class);
        BDDMockito.given(ConnectSdk.getContext()).willReturn(context);

        LocalBroadcastManager localBroadcastManager = mock(LocalBroadcastManager.class);
        BDDMockito.given(LocalBroadcastManager.getInstance(any(Context.class)))
                .willReturn(localBroadcastManager);


        final boolean newState = true;
        ConnectUtils.sendTokenStateChanged(newState);

        verify(localBroadcastManager).sendBroadcast(argThat(new ArgumentMatcher<Intent>() {
            @Override
            public boolean matches(Object argument) {
                return ((Intent) argument)
                        .getBooleanExtra("com.telenor.connect.LOGIN_STATE", false)
                        == newState;
            }
        }));
    }
}
