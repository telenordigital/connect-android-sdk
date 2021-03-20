package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.fragment.app.FragmentActivity;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.BrowserType;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.headerenrichment.ShowLoadingCallback;
import com.telenor.connect.id.Claims;
import com.telenor.connect.id.IdProvider;
import com.telenor.connect.utils.ClaimsParameterFormatter;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.CustomTabsHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class ConnectCustomTabLoginButton extends ConnectButton implements AuthenticationButton {

    private static final Uri PRE_FETCH_URL
            = Uri.parse(
            ConnectUrlHelper
                    .getConnectApiUrl()
                    .newBuilder()
                    .addPathSegment("id")
                    .addPathSegment("android-sdk-prefetch-static-resources")
                    .build()
                    .uri()
                    .toString()
    );

    private CustomTabsServiceConnection connection;
    private boolean customTabsSupported = false;
    private boolean serviceBound = false;
    private BrowserType browserType;
    private CustomTabsSession session;
    private ArrayList<String> acrValues;
    private Map<String, String> loginParameters;
    private ArrayList<String> loginScopeTokens;
    private int requestCode = 0xa987;
    private Claims claims;
    private ShowLoadingCallback showLoadingCallback;

    public ConnectCustomTabLoginButton(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        IdProvider brand = ConnectSdk.getIdProvider();
        setText(getResources().getString(R.string.com_telenor_connect_login_button_text, getResources().getString(brand.getNameKey())));
    }

    protected void authenticate() {
        ConnectSdk.authenticate(
                session,
                getParameters(),
                browserType,
                getActivity(),
                getShowLoadingCallback());
    }

    private static boolean contextIntentFilterMatchesRedirectUri(Context context) {
        final Uri parse = Uri.parse(ConnectSdk.getRedirectUri());
        final Intent intent = new Intent().setData(parse);
        final PackageManager manager = context.getPackageManager();
        final ComponentName componentName = intent.resolveActivity(manager);
        if (componentName == null) {
            return false;
        }
        return context.getPackageName().equals(componentName.getPackageName());
    }

    @NonNull
    protected Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<>();
        if (getAcrValues() != null && !getAcrValues().isEmpty()) {
            parameters.put("acr_values", TextUtils.join(" ", getAcrValues()));
        }

        if (getLoginScopeTokens() != null && !getLoginScopeTokens().isEmpty()) {
            parameters.put("scope", TextUtils.join(" ", getLoginScopeTokens()));
        }

        addClaims(parameters);

        if (getLoginParameters() != null && !getLoginParameters().isEmpty()) {
            parameters.putAll(getLoginParameters());
        }

        return parameters;
    }

    private void addClaims(Map<String, String> parameters) {
        if (getClaims() != null && getClaims().getClaimsAsSet() != null) {
            parameters.put("claims", ClaimsParameterFormatter.asJson(getClaims()));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!(getActivity() instanceof FragmentActivity)) {
            throw new ClassCastException(getActivity().toString()
                    + " must be a FragmentActivity");
        }
        String packageNameToUse = CustomTabsHelper.getPackageNameToUse(getContext());
        if (TextUtils.isEmpty(packageNameToUse)) {
            return;
        }
        connection = new WeakReferenceCustomTabsServiceConnection(new WeakReference<>(this));
        serviceBound = CustomTabsClient.bindCustomTabsService(getContext(), packageNameToUse, connection);
        boolean correctIntentFilter = contextIntentFilterMatchesRedirectUri(getContext());
        customTabsSupported = serviceBound && correctIntentFilter;
        browserType = customTabsSupported ? BrowserType.CHROME_CUSTOM_TAB : BrowserType.EXTERNAL_BROWSER;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (serviceBound) {
            getContext().unbindService(connection);
            serviceBound = false;
        }
        if (connection != null) {
            connection = null;
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Intent intent = getActivity().getIntent();
        if (ConnectSdk.hasErrorRedirectUrlCall(intent)) {
            ConnectSdk.setRandomLogSessionId();
        }
    }

    private void setSession(CustomTabsSession session) {
        this.session = session;
    }

    public ShowLoadingCallback getShowLoadingCallback() {
        return showLoadingCallback;
    }

    public void setShowLoadingCallback(ShowLoadingCallback showLoadingCallback) {
        this.showLoadingCallback = showLoadingCallback;
    }


    public ArrayList<String> getAcrValues() {
        return acrValues;
    }

    public Map<String, String> getLoginParameters() {
        return loginParameters;
    }

    public ArrayList<String> getLoginScopeTokens() {
        return loginScopeTokens;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public Claims getClaims() {
        return claims;
    }

    public void setAcrValues(String... acrValues) {
        this.acrValues = new ArrayList<>(Arrays.asList(acrValues));
    }

    public void setAcrValues(ArrayList<String> acrValues) {
        this.acrValues = acrValues;
    }

    public void setLoginScopeTokens(String... scopeTokens) {
        loginScopeTokens = new ArrayList<>(Arrays.asList(scopeTokens));
    }

    public void setLoginScopeTokens(ArrayList<String> scopeTokens) {
        loginScopeTokens = scopeTokens;
    }

    public void setExtraLoginParameters(Map<String, String> parameters) {
        loginParameters = parameters;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setClaims(Claims claims) {
        this.claims = claims;
    }

    private static class WeakReferenceCustomTabsServiceConnection extends CustomTabsServiceConnection {

        private final WeakReference<ConnectCustomTabLoginButton> weakButton;

        WeakReferenceCustomTabsServiceConnection(WeakReference<ConnectCustomTabLoginButton> weakButton) {
            this.weakButton = weakButton;
        }

        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            final ConnectCustomTabLoginButton connectCustomTabLoginButton = weakButton.get();
            if (connectCustomTabLoginButton == null) {
                return;
            }
            client.warmup(0);
            final CustomTabsSession session = client.newSession(new WeakReferenceCustomTabsCallback(weakButton));
            connectCustomTabLoginButton.setSession(session);
            if (session != null) {
                session.mayLaunchUrl(PRE_FETCH_URL, null, null);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private static class WeakReferenceCustomTabsCallback extends CustomTabsCallback {

        private final WeakReference<ConnectCustomTabLoginButton> weakButton;

        WeakReferenceCustomTabsCallback(WeakReference<ConnectCustomTabLoginButton> weakButton) {
            this.weakButton = weakButton;
        }

        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            final ConnectCustomTabLoginButton connectCustomTabLoginButton = weakButton.get();
            if (connectCustomTabLoginButton == null) {
                return;
            }
            switch (navigationEvent) {
                case CustomTabsCallback.TAB_HIDDEN:
                    connectCustomTabLoginButton.setEnabled(true);
                    return;
                case CustomTabsCallback.TAB_SHOWN:
                    connectCustomTabLoginButton.setEnabled(false);
                    return;
                default:
            }
        }
    }
}