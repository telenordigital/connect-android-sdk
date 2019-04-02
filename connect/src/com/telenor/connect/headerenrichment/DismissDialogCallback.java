package com.telenor.connect.headerenrichment;

import com.telenor.connect.utils.TurnOnMobileDataDialogAnalytics;

public interface DismissDialogCallback {
    void dismiss();
    TurnOnMobileDataDialogAnalytics getAnalytics();
}
