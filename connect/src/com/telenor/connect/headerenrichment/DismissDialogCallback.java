package com.telenor.connect.headerenrichment;

import com.telenor.connect.utils.EnableMobileDataDialogAnalytics;

public interface DismissDialogCallback {
    void dismiss();
    EnableMobileDataDialogAnalytics getAnalytics();
}
