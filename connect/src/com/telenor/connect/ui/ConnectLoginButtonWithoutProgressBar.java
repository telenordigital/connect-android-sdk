package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.BrowserType;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.CustomTabsHelper;

import java.lang.ref.WeakReference;

public class ConnectLoginButtonWithoutProgressBar extends ConnectWebViewLoginButton {

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

    private OnClickListener onClickListener;
    private CustomTabsServiceConnection connection;
    private boolean customTabsSupported = false;
    private boolean serviceBound = false;
    private BrowserType browserType;
    private CustomTabsSession session;

    public ConnectLoginButtonWithoutProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        };
        setOnClickListener(onClickListener);
    }

    @Override
    protected void authenticate() {
        if (!customTabsSupported) {
            super.authenticate();
            return;
        }
        ConnectSdk.authenticate(
                session,
                getParameters(),
                browserType,
                getActivity(),
                getShowLoadingCallback());
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        String packageNameToUse = CustomTabsHelper.getPackageNameToUse(getContext());
        if (TextUtils.isEmpty(packageNameToUse)) {
            return;
        }
        connection = new WeakReferenceCustomTabsServiceConnection(new WeakReference<>(this));
        serviceBound = CustomTabsClient.bindCustomTabsService(getContext(), packageNameToUse, connection);
        boolean correctIntentFilter = contextIntentFilterMatchesRedirectUri(getContext());
        customTabsSupported = serviceBound && correctIntentFilter;
        browserType = customTabsSupported ? BrowserType.CHROME_CUSTOM_TAB : BrowserType.WEB_VIEW;
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

    private void setSession(CustomTabsSession session) {
        this.session = session;
    }

    private static class WeakReferenceCustomTabsServiceConnection extends CustomTabsServiceConnection {

        private final WeakReference<ConnectLoginButtonWithoutProgressBar> weakButton;

        WeakReferenceCustomTabsServiceConnection(WeakReference<ConnectLoginButtonWithoutProgressBar> weakButton) {
            this.weakButton = weakButton;
        }

        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            final ConnectLoginButtonWithoutProgressBar connectLoginButtonWithoutProgressBar = weakButton.get();
            if (connectLoginButtonWithoutProgressBar == null) {
                return;
            }
            client.warmup(0);
            final CustomTabsSession session = client.newSession(new WeakReferenceCustomTabsCallback(weakButton));
            connectLoginButtonWithoutProgressBar.setSession(session);
            if (session != null) {
                session.mayLaunchUrl(PRE_FETCH_URL, null, null);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private static class WeakReferenceCustomTabsCallback extends CustomTabsCallback {

        private final WeakReference<ConnectLoginButtonWithoutProgressBar> weakButton;

        WeakReferenceCustomTabsCallback(WeakReference<ConnectLoginButtonWithoutProgressBar> weakButton) {
            this.weakButton = weakButton;
        }

        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            final ConnectLoginButtonWithoutProgressBar connectLoginButtonWithoutProgressBar = weakButton.get();
            if (connectLoginButtonWithoutProgressBar == null) {
                return;
            }
            switch (navigationEvent) {
                case CustomTabsCallback.TAB_HIDDEN:
                    connectLoginButtonWithoutProgressBar.setEnabled(true);
                    return;
                case CustomTabsCallback.TAB_SHOWN:
                    connectLoginButtonWithoutProgressBar.setEnabled(false);
                    return;
                default:
            }
        }
    }
}