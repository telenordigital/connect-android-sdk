package com.telenor.connect.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class MobileDialogAnalytics implements Serializable {

    private boolean isEnabled;
    private boolean wasShown;
    private boolean automaticButtonPressed;
    private boolean manualButtonPressed;

    public MobileDialogAnalytics(boolean isEnabled, boolean wasShown, boolean automaticButtonPressed, boolean manualButtonPressed) {
        this.isEnabled = isEnabled;
        this.wasShown = wasShown;
        this.automaticButtonPressed = automaticButtonPressed;
        this.manualButtonPressed = manualButtonPressed;
    }

    public JSONObject toJson() {
        JSONObject mobileDialogAnalytics = new JSONObject();
        try {
            mobileDialogAnalytics
                    .put("enabled", isEnabled)
                    .put("shown", wasShown)
                    .put("automaticButtonPressed", automaticButtonPressed)
                    .put("manualButtonPressed", manualButtonPressed);
        } catch (JSONException e1) {
            Log.e(ConnectUtils.LOG_TAG, "Exception making mobile dialog analytics json", e1);
            return null;
        }
        return mobileDialogAnalytics;
    }
}
