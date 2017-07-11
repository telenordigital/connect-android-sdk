package com.telenor.connect.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.google.gson.Gson;
import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectInitializationError;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.WellKnownAPI.WellKnownConfig;
import com.telenor.connect.id.ConnectTokens;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.mobileconnect.ui.OperatorSelectionActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.telenor.connect.SdkProfile.AuthFlowInitializationCallback;
import static com.telenor.connect.utils.ConnectUrlHelper.URL_ARGUMENT;

public class ConnectActivity extends FragmentActivity implements ConnectCallback {

    private final Gson gson = new Gson();
    private Fragment singleFragment;

    private final AuthFlowInitializationCallback
            authFlowInitializationCallback = new AuthFlowInitializationCallback() {
        @Override
        public void onSuccess(Uri authorizeUri, WellKnownConfig wellKnowConfig) {
            Intent intent = getIntent();
            intent.putExtra(ConnectUtils.LOGIN_AUTH_URI, authorizeUri.toString());
            intent.putExtra(ConnectUtils.WELL_KNOWN_CONFIG_EXTRA, wellKnowConfig);

            setContentView(R.layout.com_telenor_connect_activity_layout);

            FragmentManager manager = getSupportFragmentManager();
            final String fragmentTag = "SingleFragment";
            Fragment fragment = manager.findFragmentByTag(fragmentTag);

            String action = intent.getAction();
            int loadingScreen = intent.getIntExtra(
                    ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA,
                    R.layout.com_telenor_connect_default_loading_view);

            if (fragment == null) {
                fragment = new ConnectWebFragment();
                Bundle bundle = new Bundle(intent.getExtras());
                bundle.putString(ConnectUrlHelper.ACTION_ARGUMENT, action);
                bundle.putInt(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA, loadingScreen);
                if (action.equals(ConnectUtils.PAYMENT_ACTION)) {
                    bundle.putString(
                            URL_ARGUMENT,
                            intent.getStringExtra(ConnectSdk.EXTRA_PAYMENT_LOCATION));
                }
                fragment.setArguments(bundle);
                fragment.setRetainInstance(true);
                manager.beginTransaction()
                        .add(R.id.com_telenor_connect_fragment_container,
                                fragment,
                                fragmentTag)
                        .commit();
            }

            singleFragment = fragment;
        }

        @Override
        public void onError(ConnectInitializationError error) {
            Map<String, String> map = new HashMap<>();
            map.put("error", error.name());
            ConnectActivity.this.onError(map);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectSdk.getSdkProfile().initializeAuthorizationFlow(
                this, authFlowInitializationCallback);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (singleFragment != null) {
            singleFragment.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        singleFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSuccess(Object successData) {
        if (ConnectSdk.isConfidentialClient()) {
            Map<String, String> authCodeData = (Map<String, String>) successData;
            Intent intent = new Intent();
            for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            ConnectTokens connectTokens = (ConnectTokens) successData;
            Intent data = new Intent();
            String json = gson.toJson(connectTokens);
            data.putExtra(ConnectSdk.EXTRA_CONNECT_TOKENS, json);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
        ConnectSdk.getSdkProfile().onFinishAuthorization(true);
    }

    @Override
    public void onError(Object errorData) {
        Intent intent = new Intent();
        Map<String, String> authCodeData = (Map<String, String>) errorData;
        for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        ConnectSdk.getSdkProfile().onFinishAuthorization(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OperatorSelectionActivity.OPERATOR_SELECTION_REQUEST) {
            if (resultCode != RESULT_OK) {
                if (data != null && data.hasExtra("error_data")) {
                    onError(data.getExtras().get("error_data"));
                    return;
                }
                onError(Collections.EMPTY_MAP);
                return;
            }
            ConnectSdk.getSdkProfile().initializeAuthorizationFlow(
                    this, authFlowInitializationCallback);
        }
    }
}
