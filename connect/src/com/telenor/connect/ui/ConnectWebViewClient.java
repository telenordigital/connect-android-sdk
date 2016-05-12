package com.telenor.connect.ui;

import android.Manifest;
import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.AccessTokenCallback;
import com.telenor.connect.sms.SmsBroadcastReceiver;
import com.telenor.connect.sms.SmsCursorUtil;
import com.telenor.connect.sms.SmsHandler;
import com.telenor.connect.sms.SmsPinParseUtil;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.JavascriptUtil;

import java.util.List;
import java.util.regex.Pattern;

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
            = "javascript:window.AndroidInterface.processInstructions(document.getElementById"
            + "('android-instructions').innerHTML);";

    private final IntentFilter SMS_FILTER
            = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    private final Activity activity;
    private final View loadingView;
    private final View errorView;
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
            View errorView) {
        this.webView = webView;
        this.activity = activity;
        this.loadingView = loadingView;
        this.errorView = errorView;
        this.smsBroadcastReceiver = new SmsBroadcastReceiver(this);
        this.connectCallback = new AccessTokenCallback(activity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (ConnectSdk.getRedirectUri() != null
                && url.startsWith(ConnectSdk.getRedirectUri())) {
            ConnectUtils.parseAuthCode(url, connectCallback);
            return true;
        }
        if (ConnectSdk.getPaymentCancelUri() != null
                && url.startsWith(ConnectSdk.getPaymentCancelUri())) {
            activity.setResult(Activity.RESULT_CANCELED);
            activity.finish();
            return true;
        }
        if (ConnectSdk.getPaymentSuccessUri() != null
                && url.startsWith(ConnectSdk.getPaymentSuccessUri())) {
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
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
                Cursor cursor = SmsCursorUtil.getSmsCursor(activity,
                        pageLoadStarted-CHECK_FOR_SMS_BACK_IN_TIME_MILLIS);
                if (cursor.moveToFirst()) {
                    String body = cursor.getString(0);
                    handlePinFromSmsBodyIfPresent(body, instruction);
                }
            }
        }, RACE_CONDITION_DELAY_CHECK_ALREADY_RECEIVED_SMS);
    }

    private void handlePinFromSmsBodyIfPresent(String body, final Instruction instruction) {
        final String foundPin = SmsPinParseUtil.findPin(body, instruction);
        if (foundPin != null && instruction.getPinCallbackName() != null) {
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
