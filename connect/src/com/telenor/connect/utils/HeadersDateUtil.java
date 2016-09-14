package com.telenor.connect.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit.client.Header;

public class HeadersDateUtil {

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat DATE_FORMAT
            = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    @Nullable
    public static Date extractDate(@NonNull List<Header> headers) {
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase("Date")) {
                try {
                    return DATE_FORMAT.parse(header.getValue());
                } catch (ParseException e) {
                    Log.e(ConnectUtils.LOG_TAG, "Failed to call parse Date from headers. " +
                            "headers=" + headers, e);
                    return null;
                }
            }
        }
        return null;
    }

}
