package com.telenor.connect.headerenrichment;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.WebResourceResponse;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class FetchThroughMobileDataTask extends AsyncTask<String, Void, String> {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected String doInBackground(String... strings) {
        WebResourceResponse webResourceResponse = MobileDataFetcher.fetchUrlTroughCellular(strings[0]);
        if (webResourceResponse == null) {
            return null;
        }

        try {
            InputStream inputStream = webResourceResponse.getData();
            String response = IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();
            return response;
        } catch (IOException e) {
            return null;
        }
    }
}
