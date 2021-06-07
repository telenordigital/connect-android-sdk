package com.telenor.connect.headerenrichment;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class GetHeaderEnrichmentGifTask extends AsyncTask<Void, Void, HeTokenResponse> {
    private final String url;
    private final long timeout;

    public GetHeaderEnrichmentGifTask(String url, long timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (GetHeaderEnrichmentGifTask.this.getStatus() == AsyncTask.Status.RUNNING) {
                    GetHeaderEnrichmentGifTask.this.cancel(true);
                }
            }
        }, timeout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected HeTokenResponse doInBackground(Void... voids) {
        String fetchedTokenResponse = MobileDataFetcher.fetchUrlThroughCellular(url);
        if (fetchedTokenResponse == null) { return null; }

        HeTokenResponse heTokenResponse = convertHeTokenResponse(fetchedTokenResponse);
        if (heTokenResponse == null) { return null; }

        String fetchedGifResponse = MobileDataFetcher.fetchUrlThroughCellular(heTokenResponse.getGifUrl());
        if (fetchedGifResponse == null) { return null; }

        return heTokenResponse;
    }

    private static HeTokenResponse convertHeTokenResponse(String heTokenResponse) {
        String gifUrl;
        String token;
        int exp;
        try {
            JSONObject jsonResponse = new JSONObject(heTokenResponse);
            gifUrl = jsonResponse.getString("gifUrl");
            token = jsonResponse.getString("heToken");
            exp = jsonResponse.getInt("exp");
        } catch (JSONException e) {
            Log.e(ConnectUtils.LOG_TAG, "Failed to parse header-enrichment-token", e);
            ConnectSdk.sendAnalyticsData(e);
            return null;
        }
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MILLISECOND, exp);
        Date expiration = instance.getTime();
        return new HeTokenResponse(token, expiration, gifUrl);
    }
}
