package com.telenor.connect.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.WellKnownAPI;
import com.telenor.connect.sms.SmsBroadcastReceiver;
import com.telenor.connect.sms.SmsCursorUtil;
import com.telenor.connect.sms.SmsHandler;
import com.telenor.connect.sms.SmsPinParseUtil;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.JavascriptUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

public class ConnectWebViewClient extends WebViewClient implements SmsHandler, InstructionHandler {

    private static final int RACE_CONDITION_DELAY_CHECK_ALREADY_RECEIVED_SMS = 700;
    private static final int READ_RECEIVE_SMS_REQUEST_CODE = 0x2321;
    private static final String[] SMS_PERMISSIONS
            = new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS};
    private static long CHECK_FOR_SMS_BACK_IN_TIME_MILLIS = 2500;
    private static long CHECK_FOR_SMS_TIMEOUT = 60000;
    private static final int DELAY_HIDE_NATIVE_LOADING_VIEW = 50;

    private static final Pattern TD_HTTPS_PATTERN
            = Pattern.compile("^https://.*telenordigital.com(?:$|/)");
    private static final String JAVASCRIPT_PROCESSES_INSTRUCTIONS
            = "javascript:if (document.getElementById('android-instructions') !== null) {" +
            "window.AndroidInterface.processInstructions(document.getElementById('android-instructions').innerHTML)" +
            "}";

    private final IntentFilter SMS_FILTER
            = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    private final Activity activity;
    private final View loadingView;
    private final WebErrorView errorView;
    private final WebView webView;
    private final SmsBroadcastReceiver smsBroadcastReceiver;
    private final ConnectCallback connectCallback;

    private boolean waitingForPinSms = false;
    private boolean instructionsReceived;
    private long pageLoadStarted;
    private Instruction callbackInstruction;
    private List<Instruction> smsPermissionsCallbackInstructions;

    public ConnectWebViewClient(
            Activity activity,
            WebView webView,
            View loadingView,
            WebErrorView errorView,
            ConnectCallback callback) {
        this.webView = webView;
        this.activity = activity;
        this.loadingView = loadingView;
        this.errorView = errorView;
        this.connectCallback = callback;
        this.smsBroadcastReceiver = new SmsBroadcastReceiver(this);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (ConnectSdk.getRedirectUri() != null
                && url.startsWith(ConnectSdk.getRedirectUri())) {
            ConnectUtils.parseAuthCode(url, getOriginalState(), connectCallback);
            return true;
        }
        return false;
    }

    private String getOriginalState() {
        String url = activity.getIntent().getStringExtra(ConnectUtils.LOGIN_AUTH_URI);
        return Uri.parse(url).getQueryParameter("state");
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (!ConnectSdk.isCellularDataNetworkConnected()
                || ConnectSdk.isCellularDataNetworkDefault()) {
            return null;
        }
        if (shouldFetchThroughCellular(request.getUrl().toString())) {
            return fetchUrlTroughCellular(request.getUrl().toString());
        }
        return null;
    }

    public boolean shouldFetchThroughCellular(String url) {
        WellKnownAPI.WellKnownConfig wellKnownConfig =
                (WellKnownAPI.WellKnownConfig) this.activity
                .getIntent()
                .getExtras()
                .get(ConnectUtils.WELL_KNOWN_CONFIG_EXTRA);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse fetchUrlTroughCellular(String originalUrl) {
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
            interfaceToUse = shouldFetchThroughCellular(newUrl)
                    ? ConnectSdk.getCellularNetwork()
                    : ConnectSdk.getDefaultNetwork();
        } while (attempts <= ConnectSdk.MAX_REDIRECTS_TO_FOLLOW_FOR_HE);
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        String errorText = error.getDescription() + " (" + error.getErrorCode() + ")";
        errorView.setErrorText(errorText, request.getUrl().toString());
        errorView.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        String errorText = description + " (" + errorCode + ")";
        errorView.setErrorText(errorText, failingUrl);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        pageLoadStarted = System.currentTimeMillis();
        hideLoadingViewWithDelayIfHEILoading(url);
        instructionsReceived = false;
        super.onPageStarted(view, url, favicon);
    }

    private void hideLoadingViewWithDelayIfHEILoading(String url) {
        if (url != null
                && url.endsWith("/heidetect")
                && loadingView.getVisibility() == View.VISIBLE) {
            loadingView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingView.setVisibility(View.GONE);
                }
            }, DELAY_HIDE_NATIVE_LOADING_VIEW);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        loadingView.setVisibility(View.GONE);
        if (!instructionsReceived && shouldCheckPageForInstructions(url)) {
            webView.loadUrl(JAVASCRIPT_PROCESSES_INSTRUCTIONS);
        }
    }

    private boolean shouldCheckPageForInstructions(String url) {
        return TD_HTTPS_PATTERN.matcher(url).find();
    }

    @Override
    public void givenInstructions(List<Instruction> instructions) {
        if (instructionsReceived) {
            return;
        }
        instructionsReceived = true;

        if (containsSmsInstruction(instructions)) {
            if (hasPermissionToReadSms()) {
                executeInstructions(instructions);
            } else {
                smsPermissionsCallbackInstructions = instructions;
                requestSmsPermissions();
            }
        } else {
            executeInstructions(instructions);
        }
    }

    private boolean containsSmsInstruction(List<Instruction> instructions) {
        for (final Instruction instruction : instructions) {
            if (instruction.getName().equals(Instruction.PIN_INSTRUCTION_NAME)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermissionToReadSms() {
        int res1 = activity.checkCallingOrSelfPermission(Manifest.permission.RECEIVE_SMS);
        int res2 = activity.checkCallingOrSelfPermission(Manifest.permission.READ_SMS);

        return res1 == PackageManager.PERMISSION_GRANTED
                && res2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        activity.requestPermissions(SMS_PERMISSIONS, READ_RECEIVE_SMS_REQUEST_CODE);
    }

    private void executeInstructions(List<Instruction> instructions) {
        for (final Instruction instruction : instructions) {
            if (instruction.getName().equals(Instruction.PIN_INSTRUCTION_NAME)) {
                getPinFromSms(instruction);
            } else {
                runJavascriptInstruction(instruction);
            }
        }
    }

    private void runJavascriptInstruction(final Instruction instruction) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String javascript = JavascriptUtil.getJavascriptString(instruction);
                webView.loadUrl("javascript:" + javascript);
            }
        });
    }

    private void getPinFromSms(final Instruction instruction) {
        callbackInstruction = instruction;
        waitingForPinSms = true;

        subscribeToNewSms();
        handleIfSmsAlreadyArrived(instruction);
        stopGetPin(CHECK_FOR_SMS_TIMEOUT);
    }

    private void subscribeToNewSms() {
        activity.registerReceiver(smsBroadcastReceiver, SMS_FILTER);
    }

    private void stopGetPin(long delay) {
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConnectWebViewClient.this.stopGetPin();
            }
        }, delay);
    }

    private void stopGetPin() {
        waitingForPinSms = false;
        safeUnSubscribeToSmsReceived();
    }

    private void safeUnSubscribeToSmsReceived() {
        try {
            activity.unregisterReceiver(smsBroadcastReceiver);
        } catch (IllegalArgumentException ignore) {}
    }

    private void handleIfSmsAlreadyArrived(final Instruction instruction) {
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = SmsCursorUtil.getSmsCursor(activity,
                            pageLoadStarted-CHECK_FOR_SMS_BACK_IN_TIME_MILLIS);
                } catch (SecurityException e) {
                    Log.e(ConnectUtils.LOG_TAG, "Failed to acquire SMS cursor", e);
                    return;
                }

                if (cursor == null || cursor.getCount() <= 0) {
                    return;
                }

                if (!cursor.moveToFirst()) {
                    return;
                }

                String body = cursor.getString(0);
                handlePinFromSmsBodyIfPresent(body, instruction);
            }
        }, RACE_CONDITION_DELAY_CHECK_ALREADY_RECEIVED_SMS);
    }

    private synchronized void handlePinFromSmsBodyIfPresent(String body, final Instruction instruction) {
        final String foundPin = SmsPinParseUtil.findPin(body, instruction);
        if (waitingForPinSms && foundPin != null && instruction.getPinCallbackName() != null) {
            stopGetPin();
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String javascript = JavascriptUtil.getJavascriptString(
                            instruction.getPinCallbackName()
                            , "'"+foundPin+"'");
                    webView.loadUrl("javascript:" + javascript);
                }
            });
        }
    }

    @Override
    public void receivedSms(String originatingAddress, String messageBody) {
        handlePinFromSmsBodyIfPresent(messageBody, callbackInstruction);
    }

    public void onPause() {
        safeUnSubscribeToSmsReceived();
    }

    public void onResume() {
        if (waitingForPinSms) {
            subscribeToNewSms();
            handleIfSmsAlreadyArrived(callbackInstruction);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode != READ_RECEIVE_SMS_REQUEST_CODE
                || grantResults.length != 2
                || grantResults[0] != PackageManager.PERMISSION_GRANTED
                || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        executeInstructions(smsPermissionsCallbackInstructions);
        smsPermissionsCallbackInstructions = null;
    }
}
