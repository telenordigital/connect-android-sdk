package com.telenor.connect.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;

public class DetectConnection {
    public static boolean noInternetConnectionForSure(Context context) {
        final int permission = ContextCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() == null
                || !connectivityManager.getActiveNetworkInfo().isAvailable()
                || !connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
