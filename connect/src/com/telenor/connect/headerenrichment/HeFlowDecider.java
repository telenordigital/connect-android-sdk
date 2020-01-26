package com.telenor.connect.headerenrichment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import java.util.Set;

public class HeFlowDecider {

    private static final String PARAMETER_PROMPT = "prompt";
    private static final String PARAMETER_NO_SEAM = "no_seam";
    private static final String PARAMETER_HE_TOKEN = "telenordigital_he_token";

    /**
     * This method is used to specify the header enrichment flow and prevent
     * fraud possibilities. The logic that method follows is:
     *
     * - If network is mobile only: do header enrichment in SDK, if it fails - do
     * header enrichment on the backend side.
     *
     * - If network is wifi only: block header enrichment in SDK and backend.
     *
     * - If network is wifi and mobile at same time: do header enrichment in SDK, if it fails -
     * block header enrichment on the backend side.
     *
     * The way to ask backend services to skip header enrichment is to add "no_seam"
     * parameter to the request.
     *
     * @param uri final version of URI to OAuth
     * @param context current context
     * @return modified uri depending on network settings
     */
    public static Uri chooseFlow(Uri uri, Context context) {
        if (context == null) {
            return uri;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > 22) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            // NET_CAPABILITY_VALIDATED - Indicates that connectivity on this network was successfully validated.
            // NET_CAPABILITY_INTERNET - Indicates that this network should be able to reach the internet.
            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {

                // Transport - wifi and mobile network
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    if (HeLogic.failedToGetToken() || uri.getQueryParameter(PARAMETER_HE_TOKEN) == null) {
                        return uri.buildUpon().appendQueryParameter(PARAMETER_PROMPT, PARAMETER_NO_SEAM).build();
                    } else {
                        return removeUriParameter(uri, PARAMETER_PROMPT);
                    }
                }

                // Transport - wifi
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                    // Always add no_seam
                    if (hasNoSeam(uri)) {
                        return uri;
                    }
                    return uri.buildUpon().appendQueryParameter(PARAMETER_PROMPT, PARAMETER_NO_SEAM).build();
                }

                // Transport - mobile network
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                    // Never add no_seam
                    if (hasNoSeam(uri)) {
                        return removeUriParameter(uri, PARAMETER_PROMPT);
                    }
                    return uri;
                }
            }
            return uri;
        } else {
            // For devices with API < 23
            // Mobile network
            boolean isMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .isConnectedOrConnecting();
            // 3g, 4g etc
            boolean isLte = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI)
                    .isConnectedOrConnecting();
            // Wifi network
            boolean isWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .isConnectedOrConnecting();

            // Transport - wifi and mobile network
            if ((isMobile || isLte) && isWifi) {
                if (HeLogic.failedToGetToken() || uri.getQueryParameter(PARAMETER_HE_TOKEN) == null) {
                    return uri.buildUpon().appendQueryParameter(PARAMETER_PROMPT, PARAMETER_NO_SEAM).build();
                } else {
                    return removeUriParameter(uri, PARAMETER_PROMPT);
                }
            }

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

                // Transport - wifi
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (hasNoSeam(uri)) {
                        return uri;
                    }
                    return uri.buildUpon().appendQueryParameter(PARAMETER_PROMPT, PARAMETER_NO_SEAM).build();
                }

                // Transport - mobile network
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_HIPRI) {
                    // Transport - mobile network. Never add no_seam
                    if (hasNoSeam(uri)) {
                        return removeUriParameter(uri, PARAMETER_PROMPT);
                    }
                    return uri;
                }
            }
            return uri;
        }
    }

    private static boolean hasNoSeam(Uri uri) {
        return PARAMETER_NO_SEAM.equals(uri.getQueryParameter(PARAMETER_PROMPT));
    }

    private static Uri removeUriParameter(Uri uri, String key) {
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        for (String param : params) {
            if (!param.equals(key)) {
                newUri.appendQueryParameter(param, uri.getQueryParameter(param));
            }
        }
        return newUri.build();
    }

}
