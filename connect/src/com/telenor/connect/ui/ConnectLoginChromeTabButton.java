package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.ClaimsParameterFormatter;
import com.telenor.connect.utils.Validator;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class ConnectLoginChromeTabButton extends ConnectLoginButton {

    private OnClickListener onClickListener;
    private Uri uri;
    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;
    private boolean shouldPreLoad = false;
    private boolean serviceConnected = false;
    private boolean customTabsSupported = false;

    public ConnectLoginChromeTabButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);

        CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                customTabsClient = client;
                customTabsClient.warmup(0);
                customTabsSession = customTabsClient.newSession(null);
                if (shouldPreLoad && customTabsSession != null) {
                    customTabsSession.mayLaunchUrl(uri, null, null);
                }
                serviceConnected = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceConnected = false;
            }
        };

        if (!serviceConnected) {
            customTabsSupported = CustomTabsClient.bindCustomTabsService(
                    getContext(), "com.android.chrome", connection);
        }
        onClickListener = new LoginClickListener();
        setOnClickListener(onClickListener);
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void preLoad() {
        if (uri == null) {
            uri = getAuthorizeUriAndSetLastAuthState();
        }
        if (customTabsSession == null) {
            shouldPreLoad = true;
        } else {
            customTabsSession.mayLaunchUrl(uri, null, null);
        }
    }

    private Uri getAuthorizeUriAndSetLastAuthState() {
        final Map<String, String> parameters = getParameters();
        return ConnectSdk.getAuthorizeUriAndSetLastAuthState(parameters);
    }

    @NonNull
    private Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<>();
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
                        "Failed to create claims Json. claims="+getClaims(), e);
            }
        }

        if (getLoginParameters() != null && !getLoginParameters().isEmpty()){
            parameters.putAll(getLoginParameters());
        }
        return parameters;
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

            if (uri == null) {
                uri = getAuthorizeUriAndSetLastAuthState();
            }

            new CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(getActivity(), uri);
        }
    }
}
