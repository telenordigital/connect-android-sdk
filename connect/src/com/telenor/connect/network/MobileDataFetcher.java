package com.telenor.connect.network;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Network;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.WebResourceResponse;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.WellKnownAPI;
import com.telenor.connect.utils.ConnectUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

public class MobileDataFetcher {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static WebResourceResponse fetchUrlTroughCellular(String originalUrl) {
        String newUrl = originalUrl;
        int attempts = 0;
        Network interfaceToUse = ConnectSdk.getCellularNetwork();
        do {
            int responseCode;
            try {
                HttpURLConnection connection
                        = (HttpURLConnection)interfaceToUse.openConnection(new URL(newUrl));
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                responseCode = connection.getResponseCode();
                attempts += 1;
                if (responseCode != HTTP_SEE_OTHER
                        && responseCode != HTTP_MOVED_TEMP
                        && responseCode != HTTP_MOVED_PERM) {
                    // Rely on the WebView to close the input stream when finished fetching data
                    return new WebResourceResponse(
                            connection.getContentType(),
                            connection.getContentEncoding(),
                            connection.getInputStream());
                }
                newUrl = connection.getHeaderField("Location");
                // Close the input stream, but do not disconnect the connection as its socket might
                // be reused during the next request.
                connection.getInputStream().close();
            } catch (final IOException e) {
                return null;
            }
            interfaceToUse = ConnectSdk.getCellularNetwork() != null
                    ? ConnectSdk.getCellularNetwork()
                    : ConnectSdk.getDefaultNetwork();
        } while (attempts <= ConnectSdk.MAX_REDIRECTS_TO_FOLLOW_FOR_HE);
        return null;
    }
}
