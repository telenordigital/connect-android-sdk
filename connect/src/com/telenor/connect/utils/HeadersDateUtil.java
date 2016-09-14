package com.telenor.connect.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    public static Date get(@NonNull List<Header> headers) {
        try {
            return getDate(headers);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static Date getDate(@NonNull List<Header> headers) throws ParseException {
        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase("Date")) {
                return DATE_FORMAT.parse(header.getValue());
            }
        }
        return null;
    }
}
