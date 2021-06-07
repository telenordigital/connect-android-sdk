package com.telenor.connect.id;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.telenor.connect.ConnectSdk;
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

        this.receiver = new CurrentTokenStateBroadcastReceiver(this);
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

    private void addBroadcastReceiver() {
        broadcastManager.registerReceiver(receiver, loginStateChangedFilter);
    }
}
