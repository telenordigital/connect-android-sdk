package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.BrowserType;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.ClaimsParameterFormatter;
import com.telenor.connect.utils.Validator;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class ConnectLoginButton extends ConnectWebViewLoginButton {

    private OnClickListener onClickListener;
    private CustomTabsClient customTabsClient;
    private CustomTabsServiceConnection connection;
    private boolean customTabsSupported = false;
    private BrowserType browserType;

    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);

        connection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                customTabsClient = client;
                customTabsClient.warmup(0);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                customTabsClient = null;
            }
        };

        final boolean serviceBound = CustomTabsClient.bindCustomTabsService(
                getContext(), "com.android.chrome", connection);
        final boolean correctIntentFilter = contextIntentFilterMatchesRedirectUri(getContext());
        customTabsSupported = serviceBound && correctIntentFilter;
        browserType = customTabsSupported ? BrowserType.CHROME_CUSTOM_TAB : BrowserType.WEB_VIEW;
        onClickListener = new LoginClickListener();
        setOnClickListener(onClickListener);
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    private Uri getAuthorizeUriAndSetLastAuthState() {
        final Map<String, String> parameters = getParameters();
        return ConnectSdk.getAuthorizeUriAndSetLastAuthState(parameters, browserType);
    }

    @NonNull
    private Map<String, String> getParameters() {
        final Map<String, String> parameters = new HashMap<>();
        if (getAcrValues() != null && !getAcrValues().isEmpty()) {
            parameters.put("acr_values", TextUtils.join(" ", getAcrValues()));
        }

        if (getLoginScopeTokens() != null && !getLoginScopeTokens().isEmpty()) {
            parameters.put("scope", TextUtils.join(" ", getLoginScopeTokens()));
        }

        if (getClaims() != null && getClaims().getClaimsAsSet() != null) {
            try {
                parameters.put("claims", ClaimsParameterFormatter.asJson(getClaims()));
            } catch (JSONException e) {
                throw new ConnectException(
                        "Failed to create claims Json. claims=" + getClaims(), e);
            }
        }

        if (getLoginParameters() != null && !getLoginParameters().isEmpty()){
            parameters.putAll(getLoginParameters());
        }
        return parameters;
    }

    private static boolean contextIntentFilterMatchesRedirectUri(Context context) {
        final Uri parse = Uri.parse(ConnectSdk.getRedirectUri());
        final Intent intent = new Intent().setData(parse);
        final PackageManager manager = context.getPackageManager();
        final ComponentName componentName = intent.resolveActivity(manager);
        if (componentName == null) {
            return false;
        }
        return context.getClass().getName().equals(componentName.getClassName());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (connection != null) {
            getContext().unbindService(connection);
            connection = null;
        }
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.sdkInitialized();
            if (!customTabsSupported) {
                int customLoadingLayout = getCustomLoadingLayout();
                if (customLoadingLayout == NO_CUSTOM_LAYOUT) {
                    ConnectSdk.authenticate(getActivity(), getParameters(), getRequestCode());
                } else {
                    ConnectSdk.authenticate(
                            getActivity(), getParameters(), customLoadingLayout, getRequestCode());
                }

                return;
            }

            new CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(getActivity(), getAuthorizeUriAndSetLastAuthState());
        }
    }
}
