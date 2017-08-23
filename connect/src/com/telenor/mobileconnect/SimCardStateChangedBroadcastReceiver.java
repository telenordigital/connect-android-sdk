package com.telenor.mobileconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.telenor.connect.ConnectSdk;

public class SimCardStateChangedBroadcastReceiver extends BroadcastReceiver {

    private static SimCardStateChangedBroadcastReceiver sSimCardChangedReceiver =
            new SimCardStateChangedBroadcastReceiver();


    // This action is undocumented (for the time being).
    // For more info, see the following link:
    // http://stackoverflow.com/questions/10528464/how-to-monitor-sim-state-change
    private static final String SIM_STATE_CHANGED_ACTION = "android.intent.action.SIM_STATE_CHANGED";

    private static IntentFilter SIM_CARD_CHANGED_INTENT_FILTER =
            new IntentFilter(SIM_STATE_CHANGED_ACTION);

    private SimCardStateChangedBroadcastReceiver() {
    }

    public static SimCardStateChangedBroadcastReceiver getReceiver() {
        return sSimCardChangedReceiver;
    }
    public static IntentFilter getIntentFilter() {
        return SIM_CARD_CHANGED_INTENT_FILTER;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getExtras().getString("ss");
        if (state != null && "READY".equals(state)) {
            ConnectSdk.sdkReinitializeMobileConnect();
        }
    }
}
