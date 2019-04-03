package com.telenor.connect.utils;

import java.io.Serializable;

public class TurnOnMobileDataDialogAnalytics implements Serializable {

    private boolean isEnabled;
    private boolean wasShown;
    private boolean automaticButtonPressed;
    private boolean manualButtonPressed;

    public TurnOnMobileDataDialogAnalytics(
            boolean isEnabled,
            boolean wasShown,
            boolean automaticButtonPressed,
            boolean manualButtonPressed) {
        this.isEnabled = isEnabled;
        this.wasShown = wasShown;
        this.automaticButtonPressed = automaticButtonPressed;
        this.manualButtonPressed = manualButtonPressed;
    }
}
