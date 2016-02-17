package com.telenor.connect.utils;

import android.app.Activity;
import android.os.Bundle;

public class ConnectUrlHelper {

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String URL_ARGUMENT = "com.telenor.connect.URL_ARGUMENT";

    public static String getPageUrl(Bundle arguments, Activity activity) {
        if (ConnectUtils.PAYMENT_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            return arguments.getString(URL_ARGUMENT);
        } else if (ConnectUtils.LOGIN_ACTION.equals(arguments.getString(ACTION_ARGUMENT))) {
            if (activity == null
                    || activity.getIntent() == null
                    || activity.getIntent()
                    .getStringExtra(ConnectUtils.LOGIN_AUTH_URI) == null
                    || activity.getIntent()
                    .getStringExtra(ConnectUtils.LOGIN_AUTH_URI).isEmpty()) {
                throw new IllegalStateException("Required data missing for Login Action.");
            }
            return activity.getIntent().getStringExtra(ConnectUtils.LOGIN_AUTH_URI);
        }
        throw new IllegalStateException("An invalid action was used to start a Connect Activity.");
    }
}
