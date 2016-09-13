package com.telenor.connect.utils;


import android.annotation.SuppressLint;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InternetTime {

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat DATE_FORMAT
            = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    public static Date getInternetDate() throws IOException, ParseException {
        final Request request = new Request.Builder()
                .url("http://www.google.com")
                .head()
                .build();

        final Response response = new OkHttpClient().newCall(request).execute();
        final String date = response.header("Date", "-1");
        return DATE_FORMAT.parse(date);
    }

}
