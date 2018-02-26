package com.telenor.connect.id;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectSdk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectSdk.class, LocalBroadcastManager.class})
public class ConnectTokensTOStateTrackerTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    private class StateTracker extends ConnectTokensStateTracker {

        boolean onTokenStateChangedHasBeenCalled = false;
        boolean hasTokensValue;

        @Override
        protected void onTokenStateChanged(boolean hasTokens) {
            onTokenStateChangedHasBeenCalled = true;
            this.hasTokensValue = hasTokens;
        }
    }

    @Test
    public void constructingStartsTracking() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
        Context context = mock(Context.class);
        BDDMockito.given(ConnectSdk.getContext()).willReturn(context);

        PowerMockito.mockStatic(LocalBroadcastManager.class);
        LocalBroadcastManager localBroadcastManager = mock(LocalBroadcastManager.class);
        BDDMockito.given(LocalBroadcastManager.getInstance(context))
                .willReturn(localBroadcastManager);

        StateTracker stateTracker = new StateTracker();

        assertThat(stateTracker.isTracking(), is(true));
    }

    @Test
    public void constructingDoesNotCallOnTokenStateChanged() {
        PowerMockito.mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
        Context context = mock(Context.class);
        BDDMockito.given(ConnectSdk.getContext()).willReturn(context);

        PowerMockito.mockStatic(LocalBroadcastManager.class);
        LocalBroadcastManager localBroadcastManager = mock(LocalBroadcastManager.class);
        BDDMockito.given(LocalBroadcastManager.getInstance(context))
                .willReturn(localBroadcastManager);

        StateTracker stateTracker = new StateTracker();

        assertThat(stateTracker.onTokenStateChangedHasBeenCalled, is(false));
    }
}
