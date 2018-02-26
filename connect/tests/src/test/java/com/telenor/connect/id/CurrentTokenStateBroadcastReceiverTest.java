package com.telenor.connect.id;

import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class CurrentTokenStateBroadcastReceiverTest {

    @Test
    public void loginStateChangedIntentCallsListener() {
        ConnectTokensStateTracker tracker = mock(ConnectTokensStateTracker.class);

        Intent intent = new Intent("com.telenor.connect.ACTION_LOGIN_STATE_CHANGED");
        boolean value = true;
        intent.putExtra("com.telenor.connect.LOGIN_STATE", value);

        new CurrentTokenStateBroadcastReceiver(tracker)
                .onReceive(null, intent);

        verify(tracker).onTokenStateChanged(value);
    }
}
