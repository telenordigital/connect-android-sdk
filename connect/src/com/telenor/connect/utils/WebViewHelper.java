package com.telenor.connect.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.telenor.connect.sms.HtmlToAndroidInstructionsInterface;
import com.telenor.connect.ui.ConnectWebViewClient;

public class WebViewHelper {

    private static final int WEB_VIEW_TIMEOUT = 60*10*1000; // 60 seconds * 10 minutes * 1000 millis

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    // 1. HtmlToAndroidInstructionsInterface has no public fields.
    // 2. We need JS for the web page.
    public static void setupWebView(
            final WebView webView,
            ConnectWebViewClient client,
            final String pageToLoad) {

        webView.setWebViewClient(client);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        webView.addJavascriptInterface(
                new HtmlToAndroidInstructionsInterface(client), "AndroidInterface");
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        acceptAllCookies(webView);

        webView.loadUrl(pageToLoad);

        webView.postDelayed(
                new RepeatingDelayedPageReloader(webView, pageToLoad), WEB_VIEW_TIMEOUT);
    }

    private static class RepeatingDelayedPageReloader implements Runnable {

        private final WebView webView;
        private final String pageToLoad;

        public RepeatingDelayedPageReloader(final WebView webView, String pageToLoad) {
            this.webView = webView;
            this.pageToLoad = pageToLoad;
        }

        @Override
        public void run() {
            webView.loadUrl(pageToLoad);
            webView.postDelayed(
                    new RepeatingDelayedPageReloader(webView, pageToLoad), WEB_VIEW_TIMEOUT);
        }
    }

    private static void acceptAllCookies(WebView webView) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            // older versions accept third party cookies by default.
        }
    }
}
