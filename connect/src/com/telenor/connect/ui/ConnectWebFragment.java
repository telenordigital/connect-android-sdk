package com.telenor.connect.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.Button;

import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.WebViewHelper;

public class ConnectWebFragment extends Fragment {

    private ConnectWebViewClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view
                = inflater.inflate(R.layout.com_telenor_connect_web_fragment, container, false);
        final WebView webView
                = (WebView) view.findViewById(R.id.com_telenor_connect_fragment_webview);
        final ViewStub loadingView
                = (ViewStub) view.findViewById(R.id.com_telenor_connect_loading_view);
        final int loadingScreenResource = getArguments()
                .getInt(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA,
                        R.layout.com_telenor_connect_default_loading_view);
        loadingView.setLayoutResource(loadingScreenResource);
        loadingView.inflate();
        loadingView.setVisibility(View.VISIBLE);
        final String pageUrl = ConnectUrlHelper.getPageUrl(getArguments(), getActivity());
        final View errorView = view.findViewById(R.id.com_telenor_connect_error_view);
        setupErrorView(webView, loadingView, pageUrl, errorView, view);

        client = new ConnectWebViewClient(getActivity(), webView, loadingView, errorView);

        WebViewHelper.setupWebView(webView, client, pageUrl);
        return view;
    }

    private void setupErrorView(
            final WebView webView,
            final ViewStub loadingView,
            final String pageUrl,
            final View errorView,
            final View view) {
        final View loadingSpinner
                = errorView.findViewById(R.id.com_telenor_connect_error_view_loading);
        final Button tryAgain
                = (Button) errorView.findViewById(R.id.com_telenor_connect_error_view_try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingSpinner.setVisibility(View.VISIBLE);
                tryAgain.setEnabled(false);

                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingSpinner.setVisibility(View.INVISIBLE);
                        tryAgain.setEnabled(true);
                        errorView.setVisibility(View.GONE);
                        loadingView.setVisibility(View.VISIBLE);
                        webView.loadUrl(pageUrl);
                    }
                }, 1000);
            }
        });
        final Button networkSettings = (Button) errorView
                .findViewById(R.id.com_telenor_connect_error_view_network_settings);
        networkSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
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
}
