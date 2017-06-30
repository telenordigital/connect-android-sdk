package com.telenor.mobileconnect.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.telenor.connect.AbstractSdkProfile;
import com.telenor.connect.ConnectInitializationError;
import com.telenor.connect.ConnectSdk;
import com.telenor.mobileconnect.MobileConnectSdkProfile;

import java.util.HashMap;

import static com.telenor.connect.ConnectInitializationError.NO_MCC_MNC_RETURNED;
import static com.telenor.connect.utils.ConnectUtils.OPERATOR_SELECTION_URI;

public class OperatorSelectionActivity extends Activity {

    public static int OPERATOR_SELECTION_REQUEST = 0x123;
    private static String MCC_MNC_PARAM_NAME = "mcc_mnc";
    private static String MCC_MNC_PARAM_SEP = "_";

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

                Uri uri = Uri.parse(url);
                String[] mccAndMnc = getMccAndMnc(uri);

                if (mccAndMnc == null) {
                    setResult(Activity.RESULT_CANCELED, getErrorIntent(NO_MCC_MNC_RETURNED));
                    finish();
                    return true;
                }

                MobileConnectSdkProfile profile =
                        (MobileConnectSdkProfile) ConnectSdk.getSdkProfile();

                profile.initialize(
                        mccAndMnc[0],
                        mccAndMnc[1],
                        uri.getQueryParameter("subscriber_id"),
                        new AbstractSdkProfile.InitializationCallback() {
                            @Override
                            public void onSuccess() {
                                setResult(Activity.RESULT_OK, null);
                                finish();
                            }

                            @Override
                            public void onError(ConnectInitializationError error) {
                                setResult(Activity.RESULT_CANCELED, getErrorIntent(error));
                                finish();
                            }
                        }
                );
                return true;
            }
        });

        final String url = getIntent().getStringExtra(OPERATOR_SELECTION_URI);
        mWebview.loadUrl(url);
        setContentView(mWebview);
    }

    private String[] getMccAndMnc(Uri uri) {
        String mcc_mnc = uri.getQueryParameter(OperatorSelectionActivity.MCC_MNC_PARAM_NAME);
        if (mcc_mnc != null) {
            String[] mccAndMnc = mcc_mnc.split(OperatorSelectionActivity.MCC_MNC_PARAM_SEP);
            if (mccAndMnc.length == 2) {
                return mccAndMnc;
            }
        }
        return null;
    }

    private Intent getErrorIntent(ConnectInitializationError error) {
        Intent intent = new Intent();
        HashMap<String, String> errorData = new HashMap<>();
        errorData.put("error", error.name());
        intent.putExtra("error_data", errorData);
        return intent;
    }

}
