package com.telenor.connect.ui;

import android.Manifest;
import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.sms.SmsBroadcastReceiver;
import com.telenor.connect.sms.SmsCursorUtil;
import com.telenor.connect.sms.SmsHandler;
import com.telenor.connect.sms.SmsPinParseUtil;
import com.telenor.connect.id.AccessTokenCallback;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.JavascriptUtil;

import java.util.List;

public class ConnectWebViewClient extends WebViewClient implements SmsHandler, InstructionHandler {

    private static final int RACE_CONDITION_DELAY_CHECK_ALREADY_RECEIVED_SMS = 700;
    private static long CHECK_FOR_SMS_BACK_IN_TIME_MILLIS = 2500;

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
        instructionsReceived = false;
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        loadingView.setVisibility(View.GONE);
        webView.loadUrl(JAVASCRIPT_PROCESSES_INSTRUCTIONS);
    }

    @Override
    public void givenInstructions(List<Instruction> instructions) {
        if (!hasPermissionToReadSms() || instructionsReceived) {
            return;
        }

        for (final Instruction instruction : instructions) {
            if (instruction.getName().equals(Instruction.PIN_INSTRUCTION_NAME)) {
                getPinFromSms(instruction);
            } else {
                runJavascriptInstruction(instruction);
            }
        }
        instructionsReceived = true;
    }

    private boolean hasPermissionToReadSms() {
        int res1 = activity.checkCallingOrSelfPermission(Manifest.permission.RECEIVE_SMS);
        int res2 = activity.checkCallingOrSelfPermission(Manifest.permission.READ_SMS);

        return res1 == PackageManager.PERMISSION_GRANTED
                && res2 == PackageManager.PERMISSION_GRANTED;
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
        stopGetPin(instruction.getTimeout());
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
                        instruction.getConfig().getSender(),
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
        if (callbackInstruction.getConfig().getSender().equals(originatingAddress)) {
            handlePinFromSmsBodyIfPresent(messageBody, callbackInstruction);
        }
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
}
