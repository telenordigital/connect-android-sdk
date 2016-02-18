package com.telenor.connect.id;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

public abstract class ConnectTokensStateTracker {

    private static final IntentFilter loginStateChangedFilter
            = new IntentFilter(ConnectSdk.ACTION_LOGIN_STATE_CHANGED);

    private final BroadcastReceiver receiver;
    private final LocalBroadcastManager broadcastManager;

    private boolean isTracking = false;

    /**
     * The method that will be called with the token state changes.
     * @param hasTokens The new login state.
     */
    protected abstract void onTokenStateChanged(boolean hasTokens);

    public ConnectTokensStateTracker() {
        Validator.sdkInitialized();

        this.receiver = new CurrentTokenStateBroadcastReceiver();
        this.broadcastManager = LocalBroadcastManager.getInstance(ConnectSdk.getContext());

        startTrackingAccessToken();
    }

    public void startTrackingAccessToken() {
        if (isTracking) {
            return;
        }

        addBroadcastReceiver();
        isTracking = true;
    }

    public void stopTrackingAccessToken() {
        if (!isTracking) {
            return;
        }

        broadcastManager.unregisterReceiver(receiver);
        isTracking = false;
    }

    /**
     * Gets whether the tracker is tracking the current login state.
     * @return true if the tracker is tracking the current login state, false if not
     */
    public boolean isTracking() {
        return isTracking;
    }

    private class CurrentTokenStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectSdk.ACTION_LOGIN_STATE_CHANGED.equals(intent.getAction())) {
                boolean newState = intent.getBooleanExtra(ConnectUtils.LOGIN_STATE, false);
                onTokenStateChanged(newState);
            }
        }
    }

    private void addBroadcastReceiver() {
        broadcastManager.registerReceiver(receiver, loginStateChangedFilter);
    }
}
