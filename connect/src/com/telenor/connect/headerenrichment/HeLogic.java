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
import com.telenor.connect.id.IdProvider;
import com.telenor.connect.utils.ConnectUrlHelper;

import java.util.Date;
import java.util.Map;

import androidx.annotation.RequiresApi;

public class HeLogic {
    public static final boolean canNotDirectNetworkTraffic = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

    private static final long HE_TOKEN_TIMEOUT_MILLISECONDS = 10_000; // Timeout is a best guess
    // at what would be long enough to succeed and short enough for users to wait it out if
    // it should fail/time out, and do a normal flow
    private static boolean heTokenSuccess = true;
    private static HeTokenCallback heTokenCallback;
    private static boolean isHeTokenRequestOngoing;
    private static HeTokenResponse heTokenResponse;
    private static ConnectivityManager connectivityManager;
    private static volatile Network cellularNetwork;
    private static volatile Network defaultNetwork;

    public static void initializeNetworks(Context context, IdProvider provider, boolean useStaging) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connectivityManagerAvailableAndNotTooOldAndroid = connectivityManager != null
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        if (connectivityManagerAvailableAndNotTooOldAndroid) {
            initializeCellularNetwork(provider, useStaging);
            initializeDefaultNetwork();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void initializeCellularNetwork(final IdProvider provider, final boolean useStaging) {
        ConnectivityManager.NetworkCallback cellularNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                cellularNetwork = network;
                boolean noSignedInUser = ConnectSdk.getAccessToken() == null;
                if (noSignedInUser) {
                    initializeHeaderEnrichment(provider, useStaging, ConnectSdk.getLogSessionId());
                }
            }
        };
        registerCellularNetworkCallback(cellularNetworkCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void registerCellularNetworkCallback(ConnectivityManager.NetworkCallback cellularNetworkCallback) {
        NetworkRequest cellularNetworkRequest = getCellularNetworkRequest();
        try {
            connectivityManager.requestNetwork(cellularNetworkRequest, cellularNetworkCallback);
        } catch (SecurityException e) {
            cellularNetwork = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void unRegisterCellularNetworkCallback(ConnectivityManager.NetworkCallback cellularNetworkCallback) {
        try {
            connectivityManager.unregisterNetworkCallback(cellularNetworkCallback);
        } catch (SecurityException e) {
            cellularNetwork = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static NetworkRequest getCellularNetworkRequest() {
        return new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build();
    }

    private static void initializeHeaderEnrichment(IdProvider provider, boolean useStaging, String logSessionId) {
        if (canNotDirectNetworkTraffic) { return; }

        String url = ConnectUrlHelper.getHeApiUrl(provider, useStaging, logSessionId);
        GetHeaderEnrichmentGifTask getGifTask = new GetHeaderEnrichmentGifTask(url, HE_TOKEN_TIMEOUT_MILLISECONDS) {
            @Override
            protected void onPreExecute() {
                HeLogic.isHeTokenRequestOngoing = true;
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(HeTokenResponse heToken) {
                handleHeTokenResult(heToken);
            }

            @Override
            protected void onCancelled(HeTokenResponse heToken) {
                handleHeTokenResult(heToken);
            }
        };
        getGifTask.execute();
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

    public static void handleHeToken(
            final Map<String, String> parameters,
            final ShowLoadingCallback showLoadingCallback,
            final HeTokenCallback heTokenCallback,
            final String logSessionId,
            final IdProvider provider,
            final boolean useStaging,
            final DismissDialogCallback dismissDialogCallback) {
        boolean finishedUnSuccessfully = !heTokenSuccess && !isHeTokenRequestOngoing;
        boolean promptBlocksUseOfHe = parameters.containsKey("prompt") && "no_seam".equals(parameters.get("prompt"));
        boolean authenticateNow = finishedUnSuccessfully
                || promptBlocksUseOfHe
                || canNotDirectNetworkTraffic;
        if (authenticateNow) {
            callCallbacks(showLoadingCallback, heTokenCallback, dismissDialogCallback);
            return;
        }

        if (isHeTokenRequestOngoing) {
            setFutureHeTokenCallback(parameters, showLoadingCallback, heTokenCallback, logSessionId, provider, useStaging, dismissDialogCallback);
            return;
        }

        boolean heWasNeverInitialized = heTokenSuccess && heTokenResponse == null;
        boolean tokenIsExpired = heTokenResponse != null && new Date().after(heTokenResponse.getExpiration());
        if (heWasNeverInitialized || tokenIsExpired) {
            setFutureHeTokenCallback(parameters, showLoadingCallback, heTokenCallback, logSessionId, provider, useStaging, dismissDialogCallback);
            initializeHeaderEnrichment(provider, useStaging, logSessionId);
            return;
        }

        callCallbacks(showLoadingCallback, heTokenCallback, dismissDialogCallback);
    }

    private static void callCallbacks(ShowLoadingCallback showLoadingCallback, HeTokenCallback heTokenCallback, DismissDialogCallback dismissDialogCallback) {
        if (showLoadingCallback != null) {
            showLoadingCallback.stop();
        }
        if (dismissDialogCallback != null) {
            dismissDialogCallback.dismiss();
        }
        heTokenCallback.done();
    }

    private static void setFutureHeTokenCallback(
            final Map<String, String> parameters,
            final ShowLoadingCallback showLoadingCallback,
            final HeTokenCallback heTokenCallback,
            final String logSessionId,
            final IdProvider provider,
            final boolean useStaging,
            final DismissDialogCallback dismissDialogCallback) {
        HeLogic.heTokenCallback = new HeTokenCallback() {
            @Override
            public void done() {
                HeLogic.heTokenCallback = null;
                handleHeToken(parameters, showLoadingCallback, heTokenCallback, logSessionId, provider, useStaging, dismissDialogCallback);
            }
        };
    }

    private static void handleHeTokenResult(HeTokenResponse heTokenResponse) {
        isHeTokenRequestOngoing = false;
        HeLogic.heTokenResponse = heTokenResponse;
        heTokenSuccess = heTokenResponse != null;
        if (heTokenCallback != null) {
            heTokenCallback.done();
        }
    }

    public static boolean failedToGetToken() {
        return !heTokenSuccess || heTokenResponse == null;
    }

    public static HeTokenResponse getHeTokenResponse() {
        return heTokenResponse;
    }

    public static boolean isCellularDataNetworkConnected() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo;
        if (canNotDirectNetworkTraffic) {
            networkInfo = getCellularNetworkInfo();
        } else {
            if (cellularNetwork == null) {
                return false;
            }
            networkInfo = connectivityManager.getNetworkInfo(cellularNetwork);
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    public static NetworkInfo getCellularNetworkInfo() {
        if (connectivityManager == null) { return null; }
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isCellularDataNetworkDefault() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static NetworkInfo getActiveNetworkInfo() {
        if (connectivityManager == null) { return null; }
        return connectivityManager.getActiveNetworkInfo();
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
