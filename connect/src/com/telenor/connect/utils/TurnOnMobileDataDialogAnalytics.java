package com.telenor.connect.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class TurnOnMobileDataDialogAnalytics implements Serializable {

    private boolean isEnabled;
    private boolean wasShown;
    private boolean automaticButtonPressed;
    private boolean manualButtonPressed;

    public TurnOnMobileDataDialogAnalytics(boolean isEnabled, boolean wasShown, boolean automaticButtonPressed, boolean manualButtonPressed) {
        this.isEnabled = isEnabled;
        this.wasShown = wasShown;
        this.automaticButtonPressed = automaticButtonPressed;
        this.manualButtonPressed = manualButtonPressed;
    }

    public JSONObject toJson() {
        JSONObject enableMobileDataDialogAnalytics = new JSONObject();
        try {
            enableMobileDataDialogAnalytics
                    .put("enabled", isEnabled)
                    .put("shown", wasShown)
                    .put("automaticButtonPressed", automaticButtonPressed)
                    .put("manualButtonPressed", manualButtonPressed);
        } catch (JSONException e1) {
            Log.e(ConnectUtils.LOG_TAG, "Exception making mobile dialog analytics json", e1);
            return null;
        }
        return enableMobileDataDialogAnalytics;
    }
}
