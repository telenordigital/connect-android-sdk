package com.telenor.connect.headerenrichment;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUrlHelper;

import java.util.Date;
import java.util.Map;

public class HeLogic {
    static final int MAX_REDIRECTS_TO_FOLLOW_FOR_HE = 5;

    private static final long HE_TOKEN_TIMEOUT_MILLISECONDS = 10_000;
    private static boolean heTokenSuccess = true;
    private static HeTokenCallback heTokenCallback;
    private static boolean isHeTokenRequestOngoing;
    private static HeToken heToken;
    private static ConnectivityManager connectivityManager;
    private static volatile Network cellularNetwork;
    private static volatile Network defaultNetwork;

    public static void initializeNetworks(Context context, boolean useStaging) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initializeCellularNetwork(useStaging);
            initializeDefaultNetwork();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void initializeCellularNetwork(final boolean useStaging) {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        try {
            connectivityManager.requestNetwork(
                    networkRequest,
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            cellularNetwork = network;
                            boolean noSignedInUser = ConnectSdk.getAccessToken() == null;
                            if (noSignedInUser) {
                                HeLogic.initializeHeaderEnrichment(useStaging, ConnectSdk.getLogSessionId());
                            }
                        }
                    }
            );
        } catch (SecurityException e) {
            cellularNetwork = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void initializeDefaultNetwork() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        try {
            connectivityManager.requestNetwork(
                    networkRequest,
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            defaultNetwork = network;
                        }
                    }
            );
        } catch (SecurityException e) {
            defaultNetwork = null;
        }
    }

    private static void initializeHeaderEnrichment(boolean useStaging, String logSessionId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        String url = ConnectUrlHelper.getHeApiUrl(useStaging, logSessionId);
        GetHeaderEnrichmentGifTask getGifTask = new GetHeaderEnrichmentGifTask(url, HE_TOKEN_TIMEOUT_MILLISECONDS) {
            @Override
            protected void onPreExecute() {
                HeLogic.isHeTokenRequestOngoing = true;
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(HeToken heToken) {
                handleHeTokenResult(heToken);
            }

            @Override
            protected void onCancelled(HeToken heToken) {
                handleHeTokenResult(heToken);
            }
        };
        getGifTask.execute();
    }

    public static void handleHeToken(
            final Map<String, String> parameters,
            final ShowLoadingCallback showLoadingCallback,
            final HeTokenCallback heTokenCallback,
            final String logSessionId,
            final boolean useStaging) {
        boolean finishedUnSuccessfully = !heTokenSuccess && !isHeTokenRequestOngoing;
        boolean promptBlocksUseOfHe = parameters.containsKey("prompt") && "no_seam".equals(parameters.get("prompt"));
        boolean authenticateNow = finishedUnSuccessfully || promptBlocksUseOfHe;
        if (authenticateNow) {
            callCallbacks(showLoadingCallback, heTokenCallback);
            return;
        }

        if (isHeTokenRequestOngoing) {
            setFutureHeTokenCallback(parameters, showLoadingCallback, heTokenCallback, logSessionId, useStaging);
            return;
        }

        boolean heWasNeverInitialized = heTokenSuccess && heToken == null;
        boolean tokenIsExpired = heToken != null && new Date().after(heToken.getExpiration());
        if (heWasNeverInitialized || tokenIsExpired) {
            setFutureHeTokenCallback(parameters, showLoadingCallback, heTokenCallback, logSessionId, useStaging);
            initializeHeaderEnrichment(useStaging, logSessionId);
            return;
        }

        callCallbacks(showLoadingCallback, heTokenCallback);
    }

    private static void callCallbacks(ShowLoadingCallback showLoadingCallback, HeTokenCallback heTokenCallback) {
        if (showLoadingCallback != null) {
            showLoadingCallback.stop();
        }
        heTokenCallback.done();
    }

    private static void setFutureHeTokenCallback(
            final Map<String, String> parameters,
            final ShowLoadingCallback showLoadingCallback,
            final HeTokenCallback heTokenCallback,
            final String logSessionId,
            final boolean useStaging) {
        HeLogic.heTokenCallback = new HeTokenCallback() {
            @Override
            public void done() {
                HeLogic.heTokenCallback = null;
                handleHeToken(parameters, showLoadingCallback, heTokenCallback, logSessionId, useStaging);
            }
        };
    }

    private static void handleHeTokenResult(HeToken heToken) {
        isHeTokenRequestOngoing = false;
        HeLogic.heToken = heToken;
        heTokenSuccess = heToken != null;
        if (heTokenCallback != null) {
            heTokenCallback.done();
        }
    }

    public static boolean failedToGetToken() {
        return !heTokenSuccess || heToken == null;
    }

    public static HeToken getHeToken() {
        return heToken;
    }

    public static boolean isCellularDataNetworkConnected() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        } else {
            if (cellularNetwork == null) {
                return false;
            }
            networkInfo = connectivityManager.getNetworkInfo(cellularNetwork);
        }
        return (networkInfo != null) && networkInfo.isConnected();
    }

    public static boolean isCellularDataNetworkDefault() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Network getCellularNetwork() {
        return cellularNetwork;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Network getDefaultNetwork() {
        return defaultNetwork;
    }
}
