package com.telenor.connect.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.WebViewHelper;

public class ConnectWebFragment extends Fragment {

    public static final String WEB_VIEW_URL = "WEB_VIEW_URL";
    private ConnectWebViewClient client;
    private WebView webView;

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

        webView = (WebView) view.findViewById(R.id.com_telenor_connect_fragment_webview);
        client = new ConnectWebViewClient(getActivity(), webView, loadingView, errorView);

        String pageUrl;
        String defaultUrl = ConnectUrlHelper.getPageUrl(getArguments(), getActivity());
        pageUrl = defaultUrl;
        Log.w(ConnectUtils.LOG_TAG, "pageUrl is: " + pageUrl);
        if (savedInstanceState != null) {
            Log.w(ConnectUtils.LOG_TAG, "savedInstanceState: " + savedInstanceState);
            pageUrl = savedInstanceState.getString(WEB_VIEW_URL, defaultUrl);
            Log.w(ConnectUtils.LOG_TAG, "pageUrl is now: " + pageUrl);
        }
        WebViewHelper.setupWebView(webView, client, pageUrl);
        return view;
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        client.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String url = webView.getUrl();
        if (url.endsWith("/verify-phone")) {
            url += "?suppresspin=true";
        }
        outState.putString(WEB_VIEW_URL, url);

    }
}
