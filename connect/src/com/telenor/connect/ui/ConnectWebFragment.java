package com.telenor.connect.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.WebViewHelper;

public class ConnectWebFragment extends Fragment {

    public static final String ACTION_ARGUMENT = "com.telenor.connect.ACTION_ARGUMENT";
    public static final String URL_ARGUMENT = "com.telenor.connect.URL_ARGUMENT";

    private ConnectWebViewClient client;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.com_telenor_connect_web_fragment, container, false);
        View errorView = view.findViewById(R.id.com_telenor_connect_error_view);
        View loadingView = view.findViewById(R.id.com_telenor_connect_loading_view);
        loadingView.setVisibility(View.VISIBLE);

        WebView webView = (WebView) view.findViewById(R.id.com_telenor_connect_fragment_webview);
        client = new ConnectWebViewClient(getActivity(), webView, loadingView, errorView);

        acceptAllCookies(webView);

        WebViewHelper.setupWebView(webView, client, getPageUrl());

        return view;
    }

    private void acceptAllCookies(WebView webView) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            // older versions accept third party cookies by default.
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        client.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        client.onResume();
    }

    private String getPageUrl() {
        if (getArguments().getString(ACTION_ARGUMENT).equals(ConnectUtils.PAYMENT_ACTION)) {
            return getArguments().getString(URL_ARGUMENT);
        } else if (getArguments().getString(ACTION_ARGUMENT).equals(ConnectUtils.LOGIN_ACTION)) {
            if (getActivity() == null
                    || getActivity().getIntent() == null
                    || getActivity().getIntent()
                            .getStringExtra(ConnectUtils.LOGIN_AUTH_URI) == null
                    || getActivity().getIntent()
                            .getStringExtra(ConnectUtils.LOGIN_AUTH_URI).isEmpty()) {
                throw new IllegalStateException("Required data missing for Login Action.");
            }
            return getActivity().getIntent().getStringExtra(ConnectUtils.LOGIN_AUTH_URI);
        }
        throw new IllegalStateException("An invalid action was used to start a Connect Activity.");
    }



}
