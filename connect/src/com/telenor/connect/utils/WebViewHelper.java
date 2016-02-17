package com.telenor.connect.utils;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.telenor.connect.sms.HtmlToAndroidInstructionsInterface;
import com.telenor.connect.ui.ConnectWebViewClient;

public class WebViewHelper {

    public static void setupWebView(
            WebView webView,
            ConnectWebViewClient client,
            String pageToLoad) {

        webView.setWebViewClient(client);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings()
                    .setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
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
