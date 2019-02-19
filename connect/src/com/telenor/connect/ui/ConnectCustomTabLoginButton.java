package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.BrowserType;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.CustomTabsHelper;

import java.lang.ref.WeakReference;

/**
 * Uses custom tabs, unless not available. If not available falls back to ConnectWebViewLoginButton
 * logic.
 */
public class ConnectCustomTabLoginButton extends ConnectWebViewLoginButton {

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

    public ConnectCustomTabLoginButton(final Context context, AttributeSet attributeSet) {
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