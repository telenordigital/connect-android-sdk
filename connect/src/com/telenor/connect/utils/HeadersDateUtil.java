package com.telenor.connect.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Headers;

public class HeadersDateUtil {

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat DATE_FORMAT
            = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    @Nullable
    public static Date extractDate(@NonNull Headers headers) {
        for (String name : headers.names()) {
            if ("Date".equalsIgnoreCase(name)) {
                try {
                    return DATE_FORMAT.parse(headers.get(name));
                } catch (ParseException e) {
                    Log.w(ConnectUtils.LOG_TAG, "Failed to call parse Date from headers. " +
                            "headers=" + headers, e);
                    return null;
                }
            }
        }
        return null;
    }

}
