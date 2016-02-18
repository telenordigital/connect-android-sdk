package com.telenor.connect.id;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;

public class CurrentTokenStateBroadcastReceiver extends BroadcastReceiver {

    private final ConnectTokensStateTracker listener;

    public CurrentTokenStateBroadcastReceiver(ConnectTokensStateTracker listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectSdk.ACTION_LOGIN_STATE_CHANGED.equals(intent.getAction())) {
            boolean newState = intent.getBooleanExtra(ConnectUtils.LOGIN_STATE, false);
            listener.onTokenStateChanged(newState);
        }
    }
}
