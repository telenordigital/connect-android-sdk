package com.telenor.connect.headerenrichment;

import org.json.JSONObject;

public interface DismissDialogCallback {
    void dismiss();
    JSONObject getAnalytics();
}
