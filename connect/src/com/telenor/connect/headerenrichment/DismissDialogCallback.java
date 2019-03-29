package com.telenor.connect.headerenrichment;

import com.telenor.connect.utils.MobileDialogAnalytics;

public interface DismissDialogCallback {
    void dismiss();
    MobileDialogAnalytics getAnalytics();
}
