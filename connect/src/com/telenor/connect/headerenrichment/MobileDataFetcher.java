package com.telenor.connect.headerenrichment;

import android.net.Network;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.WellKnownAPI;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

public class MobileDataFetcher {

    private static final int MAX_REDIRECTS_TO_FOLLOW_FOR_HE = 5;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String fetchUrlThroughCellular(String url) {
        WebResourceResponse webResourceResponse = MobileDataFetcher.fetchWebResourceResponse(url, false);
        if (webResourceResponse == null) {
            return null;
        }

        int statusCode = webResourceResponse.getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static WebResourceResponse fetchWebResourceResponse(String originalUrl, boolean allowedToToggleNetworkToUse) {
        String newUrl = originalUrl;
        int attempts = 0;
        Network interfaceToUse = HeLogic.getCellularNetwork();
        if (interfaceToUse == null) {
            return null;
        }
        do {
            try {
                HttpURLConnection connection
                        = (HttpURLConnection)interfaceToUse.openConnection(new URL(newUrl));
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                int responseCode = connection.getResponseCode();
                attempts += 1;
                if (responseCode != HTTP_SEE_OTHER
                        && responseCode != HTTP_MOVED_TEMP
                        && responseCode != HTTP_MOVED_PERM) {
                    // Rely on the WebView to close the input stream when finished fetching data
                    return new WebResourceResponse(
                            connection.getContentType(),
                            connection.getContentEncoding(),
                            connection.getResponseCode(),
                            connection.getResponseMessage(),
                            getHeadersAsCommaJoinedList(connection.getHeaderFields()),
                            connection.getInputStream());
                }
                newUrl = connection.getHeaderField("Location");
                // Close the input stream, but do not disconnect the connection as its socket might
                // be reused during the next request.
                connection.getInputStream().close();
            } catch (final IOException e) {
                return null;
            }
            if (allowedToToggleNetworkToUse) {
                interfaceToUse = shouldFetchThroughCellular(newUrl)
                        ? HeLogic.getCellularNetwork()
                        : HeLogic.getDefaultNetwork();
            }
        } while (attempts <= MAX_REDIRECTS_TO_FOLLOW_FOR_HE);
        return null;
    }

    private static Map<String, String> getHeadersAsCommaJoinedList(Map<String, List<String>> headerFields) {
        Map<String, String> result = new HashMap<>();
        for (String header : headerFields.keySet()) {
            List<String> strings = headerFields.get(header);
            if (strings == null || strings.isEmpty()) {
                continue;
            }
            String commaJoinedList = TextUtils.join(",", strings);
            result.put(header, commaJoinedList);
        }
        return result;
    }

    public static boolean shouldFetchThroughCellular(String url) {
        WellKnownAPI.WellKnownConfig wellKnownConfig = ConnectSdk.getWellKnownConfig();
        if (wellKnownConfig == null ||
                (wellKnownConfig.getNetworkAuthenticationTargetIps().isEmpty()
                        && wellKnownConfig.getNetworkAuthenticationTargetUrls().isEmpty())) {
            return false;
        }
        if (!wellKnownConfig.getNetworkAuthenticationTargetUrls().isEmpty()) {
            for (String urlPrefix : wellKnownConfig.getNetworkAuthenticationTargetUrls()) {
                if (url.contains(urlPrefix)) {
                    return true;
                }
            }
            return false;
        } else {
            String hostIp;
            try {
                String host = (new URL(url)).getHost();
                hostIp = InetAddress.getByName(host).getHostAddress();
            } catch (MalformedURLException | UnknownHostException e) {
                return false;
            }
            return wellKnownConfig
                    .getNetworkAuthenticationTargetIps()
                    .contains(hostIp);
        }
    }
}
