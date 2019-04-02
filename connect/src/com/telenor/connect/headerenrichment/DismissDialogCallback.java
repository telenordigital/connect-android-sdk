package com.telenor.connect.headerenrichment;

import com.telenor.connect.utils.TurnOnMobileDataAnalytics;

public interface DismissDialogCallback {
    void dismiss();
    TurnOnMobileDataAnalytics getAnalytics();
}
