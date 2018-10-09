package com.telenor.connect.headerenrichment;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.telenor.connect.utils.ConnectUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class GetHeaderEnrichmentGifTask extends AsyncTask<String, Void, HeToken> {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected HeToken doInBackground(String... strings) {
        long startTime = System.currentTimeMillis();
        FetchThroughMobileDataTask fetchToken = new FetchThroughMobileDataTask();
        fetchToken.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strings[0]);
        String getTokenResponse;
        try {
            getTokenResponse = fetchToken.get(AuthEventHandler.TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.w(ConnectUtils.LOG_TAG, "Failed to fetch header-enrichment-token", e);
            return null;
        } catch (InterruptedException e) {
            Log.w(ConnectUtils.LOG_TAG, "Interrupted fetching header-enrichment-token", e);
            return null;
        } catch (TimeoutException e) {
            Log.w(ConnectUtils.LOG_TAG, "Timed out fetching header-enrichment-token", e);
            return null;
        }
        if (getTokenResponse == null) {
            return null;
        }

        HeToken heToken = getHeToken(getTokenResponse);
        if (heToken == null) {
            return null;
        }

        long timeUsedSoFar = System.currentTimeMillis() - startTime;
        long remainingTimeout = AuthEventHandler.TIMEOUT_MILLISECONDS - timeUsedSoFar;
        if (remainingTimeout < 0) {
            return null;
        }
        FetchThroughMobileDataTask fetchGif = new FetchThroughMobileDataTask();
        fetchGif.execute(heToken.getGifUrl());
        try {
            fetchGif.get(remainingTimeout, TimeUnit.MILLISECONDS);
            return heToken;
        } catch (ExecutionException e) {
            Log.w(ConnectUtils.LOG_TAG, "Failed fetching gifUrl", e);
            return null;
        } catch (InterruptedException e) {
            Log.w(ConnectUtils.LOG_TAG, "Interrupted fetching gifUrl", e);
            return null;
        } catch (TimeoutException e) {
            Log.w(ConnectUtils.LOG_TAG, "Timed out fetching gifUrl", e);
            return null;
        }
    }

    private static HeToken getHeToken(String getTokenResponse) {
        String gifUrl;
        String token;
        int exp;
        try {
            JSONObject jsonResponse = new JSONObject(getTokenResponse);
            gifUrl = jsonResponse.getString("gifUrl");
            token = jsonResponse.getString("token");
            exp = jsonResponse.getInt("exp");
        } catch (JSONException e) {
            Log.w(ConnectUtils.LOG_TAG, "Failed to parse header-enrichment-token", e);
            return null;
        }
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MILLISECOND, exp);
        Date expiration = instance.getTime();
        return new HeToken(token, expiration, gifUrl);
    }

//    @Override
//    protected void onPostExecute(HeToken heToken) {
//        Toast.makeText(ConnectSdk.getContext(), "Success!", Toast.LENGTH_LONG).show();
////            String url = "https://grumpy-turkey-95.localtunnel.me/id/signin?gui_config=eyJraWQiOiJ1aV9jYWxsYmFjayIsImFsZyI6IlJTMjU2In0.eyJsb2MiOiJlbiIsImxzaSI6IjA2MmJmMGJhLTU3OGYtNDA2ZC05OTIxLWUyNDY4YjY3N2FiMSIsImxvaCI6W10sInNkdiI6IiIsInVzYyI6ImNvbm5lY3RfaWQiLCJwdWwiOmZhbHNlLCJzc2kiOm51bGwsImhlZSI6dHJ1ZSwicHV0IjoibXNpc2RuIiwibWNkIjpmYWxzZSwiYWNyIjpbIjEiXSwiYnJkIjoiZ29sZGVucnllIiwic2VuIjoic2ltdWxhdG9yIiwicmVzIjoiQklHUkFORE9NU1RSSU5HT0ZHQVJCQUdFVEhBVFdJTExNRUFOTk9USElOR1RPWU9VIiwiYXB0IjoibmF0aXZlIiwidHBhIjpmYWxzZSwib2xkIjpmYWxzZSwidGF1IjpmYWxzZSwidWF0IjpudWxsLCJhdWMiOiJ0ZWxlbm9yZGlnaXRhbC1jb25uZWN0ZXhhbXBsZS1hbmRyb2lkOlwvXC9vYXV0aDJjYWxsYmFjayIsImxwciI6ZmFsc2UsImxobCI6ZmFsc2UsImVzYyI6W10sInZwciI6ZmFsc2UsImJ1biI6IlNpbXVsYXRlZEJ1TmFtZSIsInNscyI6ZmFsc2UsInNsdSI6ZmFsc2UsImNpZCI6ImU5OTgzZWZmLWRlMTktNDQyZi05M2FjLWRlYWJkNzViYjAxZSJ9.f186-zkl35eC-Yw3-Aj9jZaekRRmHI4nZts4jWTvHgP6qkHV5nTdwel8Z_KbhDrLbqNt18vSLdinEQLEUVve5-Ghldm3_r1zZQVUh5xHbncEYYkli8Bi_0vTHQgDK7sOnpjP75r9vkTzLQFhNhiEMTloSFkczt1mxooHiNC0ZoHmydUYV9EKoa8E68fFqEMfEmzPDGEDcKod9f3GWAu1IliEmfPOqN1ZST25DsdZgK3xu60WduSkz1eU3paLo2jaDPwkvFKw0Pz_J5hEf1yovmed4hzy7oXNYpaDv5u5WDlihu_wbEgLWFF3lTDqCtGNLh3xRlqru98gZELuDWh24w";
//        String url = "https://grumpy-turkey-95.localtunnel.me/id/signin?gui_config=eyJraWQiOiJ1aV9jYWxsYmFjayIsImFsZyI6IlJTMjU2In0.eyJsb2MiOiJlbiIsImxzaSI6ImQ4MjMyNjgyLWYxMDAtNDAyYy05ZDc2LTMxMTcwYzhmNzZiYSIsImxvaCI6W10sInNkdiI6IiIsInVzYyI6ImNvbm5lY3RfaWQiLCJwdWwiOmZhbHNlLCJzc2kiOm51bGwsImhlZSI6dHJ1ZSwicHV0IjoibXNpc2RuIiwibWNkIjpmYWxzZSwiYWNyIjpbIjIiXSwiYnJkIjoiZ29sZGVucnllIiwic2VuIjoic2ltdWxhdG9yIiwicmVzIjoiQklHUkFORE9NU1RSSU5HT0ZHQVJCQUdFVEhBVFdJTExNRUFOTk9USElOR1RPWU9VIiwiYXB0Ijoid2ViIiwidHBhIjpmYWxzZSwib2xkIjpmYWxzZSwidGF1IjpmYWxzZSwidWF0IjpudWxsLCJhdWMiOiJcL2lkXC9kZWJ1Z1wvbGFuZGluZyIsImxwciI6ZmFsc2UsImxobCI6ZmFsc2UsImVzYyI6W10sInZwciI6ZmFsc2UsImJ1biI6IlNpbXVsYXRlZEJ1TmFtZSIsInNscyI6ZmFsc2UsInNsdSI6ZmFsc2UsImNpZCI6IjM0NjczMjA3LTZlZjEtNDc2NS04ZGViLTNmZDRjY2RmNGM1MiJ9.y_eCDW0yKK-UUcjKi9SNAwtVbREUrgwRaE25zFcilarSp0RBAQM_2hkrjy5hk-sNp01TZn87sKEbH_hgyRlAQheV8oK_hZEP5M_9ytcjoJC7YruwF2vn0gKIg5yqNLFSwuRspqRPQxxDTTrBI3HBGUbP4eB6kAIG5MpvXqETib9gmpsKBrsR9JR5KvEe9T5VxlLVUWXR0VgCX3rs6-VeLRhFbUa_UDk5XKhdQvlJkg37UGtDmOExcoaQHuTorwvUnTR7hz5EivyFEP07aIJxJUhA2_XmVJFnY1gY0PZ2ccOac2ZFdxLBTfO9k2YF8nSq3HicZJxEuLhoLgqvamHFpg";
//        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//        CustomTabsIntent customTabsIntent = builder.build();
//        customTabsIntent.launchUrl(ConnectSdk.getContext(), Uri.parse(url));
//    }
}
