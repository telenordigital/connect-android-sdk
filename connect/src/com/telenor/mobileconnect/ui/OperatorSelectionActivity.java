package com.telenor.mobileconnect.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.telenor.connect.ConnectSdk;

import static com.telenor.connect.utils.ConnectUtils.OPERATOR_SELECTION_URI;

public class OperatorSelectionActivity extends Activity {

    public static int OPERATOR_SELECTION_REQUEST = 0x123;

    private WebView mWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebview  = new WebView(this);
        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

        mWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (ConnectSdk.getRedirectUri() == null
                    || !url.startsWith(ConnectSdk.getRedirectUri())) {
                    return false;
                }
                final Uri uri = Uri.parse(url);
                final String mcc_mnc = uri.getQueryParameter("mcc_mnc");
                setResult(Activity.RESULT_CANCELED, null);
                if (mcc_mnc != null) {
                    final String[] mccAndMnc = mcc_mnc.split("_");
                    if (mccAndMnc.length == 2) {
                        ConnectSdk.getSdkProfile().setValue("mcc", mccAndMnc[0]);
                        ConnectSdk.getSdkProfile().setValue("mnc", mccAndMnc[1]);
                        ConnectSdk.getSdkProfile().setValue("subscriber_id",
                                uri.getQueryParameter("subscriber_id"));
                        setResult(Activity.RESULT_OK, null);
                    }
                }
                finish();
                return true;
            }
        });

        final String url = getIntent().getStringExtra(OPERATOR_SELECTION_URI);

        mWebview.loadUrl(url);
        setContentView(mWebview);
    }
}
