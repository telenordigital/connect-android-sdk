package com.telenor.connect.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;

public class ConnectWebFragment extends Fragment {
    private Uri url;
    private static View sLoadingView;
    protected static WebView sWebView;

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String URL_ARGUMENT = "com.telenor.connect.URL_ARGUMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.com_telenor_connect_web_fragment, container, false);
        sLoadingView = view.findViewById(R.id.com_telenor_connect_loading_view);
        sWebView = (WebView) view.findViewById(R.id.com_telenor_connect_fragment_webview);
        sWebView.setWebViewClient(new ConnectWebViewClient());
        sWebView.setVerticalScrollBarEnabled(true);
        sWebView.setHorizontalScrollBarEnabled(false);
        sWebView.getSettings().setJavaScriptEnabled(true);
        sWebView.getSettings().setSaveFormData(false);
        sWebView.setFocusable(true);
        sWebView.setFocusableInTouchMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sWebView.setWebContentsDebuggingEnabled(true);
        }
        sWebView.loadUrl(getPageUrl());
        CookieManager.getInstance().setAcceptCookie(true);
        return view;
    }

    private String getAuthorizeUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(ConnectSdk.getConnectApiUrl().toString())
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", ConnectSdk.getClientId())
                .appendQueryParameter("redirect_uri", ConnectSdk.getRedirectUri())
                .appendQueryParameter("scope", TextUtils.join(" ", getLoginScopeTokens()))
                .appendQueryParameter("acr_values", ConnectSdk.getAcrValue());
        return builder.build().toString();
    }

    private String getPageUrl() {
        if (getArguments().getString(ACTION_ARGUMENT).equals(ConnectUtils.LOGIN_ACTION)) {
            return getAuthorizeUri();
        } else if (getArguments().getString(ACTION_ARGUMENT).equals(ConnectUtils.PAYMENT_ACTION)) {
            return getArguments().getString(URL_ARGUMENT);
        }
        throw new IllegalStateException();
    }

    private ArrayList<String> getLoginScopeTokens() {
        if (getArguments().getString(ACTION_ARGUMENT).equals(ConnectUtils.LOGIN_ACTION)) {
            return getArguments().getStringArrayList(ConnectUtils.LOGIN_SCOPE_TOKENS);
        }
        throw new IllegalStateException("Cannot log in without scope tokens.");
    }

    private class ConnectWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(ConnectSdk.getRedirectUri())) {
                ConnectUtils.parseAuthCode(url, new ConnectCallback() {
                    @Override
                    public void onSuccess(Object successData) {
                        Validator.notNullOrEmpty(successData.toString(), "code");
                        ConnectIdService.getAccessTokenFromCode(successData.toString(),
                                new ConnectCallback() {
                            @Override
                            public void onSuccess(Object successData) {
                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();
                            }

                            @Override
                            public void onError(Object errorData) {
                                Log.e(ConnectUtils.LOG_TAG, errorData.toString());
                                getActivity().setResult(Activity.RESULT_CANCELED);
                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onError(Object errorData) {
                        Log.e(ConnectUtils.LOG_TAG, errorData.toString());
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finish();
                    }
                });
                return true;
            }
            if (ConnectSdk.isPaymentEnabled()) {
                if (url.startsWith(ConnectSdk.getPaymentCancelUri())) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                    return true;
                }
                if (url.startsWith(ConnectSdk.getPaymentSuccessUri())) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            sLoadingView.setVisibility(View.GONE);
        }
    }
}
