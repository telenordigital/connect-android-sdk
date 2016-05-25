package com.telenor.connect.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;

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
        View errorView = view.findViewById(R.id.com_telenor_connect_error_view);
        ViewStub loadingView = (ViewStub) view.findViewById(R.id.com_telenor_connect_loading_view);
        final int loadingScreenResource = getArguments()
                .getInt(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA,
                        R.layout.com_telenor_connect_default_loading_view);
        loadingView.setLayoutResource(loadingScreenResource);
        loadingView.inflate();
        loadingView.setVisibility(View.VISIBLE);

        WebView webView = (WebView) view.findViewById(R.id.com_telenor_connect_fragment_webview);
        client = new ConnectWebViewClient(getActivity(), webView, loadingView, errorView);

        String pageUrl = ConnectUrlHelper.getPageUrl(getArguments(), getActivity());
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
}
