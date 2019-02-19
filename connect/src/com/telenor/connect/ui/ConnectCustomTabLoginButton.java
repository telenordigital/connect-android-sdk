package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.BrowserType;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.CustomTabsHelper;

/**
 * Uses custom tabs, unless not available. If not available falls back to ConnectWebViewLoginButton
 * logic.
 */
public class ConnectCustomTabLoginButton extends ConnectWebViewLoginButton {

    final private CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
        @Override
        public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient client) {
            client.warmup(0);
            final CustomTabsSession session = client.newSession(null);
            ConnectCustomTabLoginButton.this.session = session;
            session.mayLaunchUrl(ConnectUrlHelper.getPreFetchUrl(), null, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            session = null;
        }
    };

    private boolean customTabsSupported = false;
    private boolean serviceBound = false;
    private BrowserType browserType;
    private CustomTabsSession session;

    public ConnectCustomTabLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void authenticate() {
        setEnabled(false);
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        String packageNameToUse = CustomTabsHelper.getPackageNameToUse(getContext());
        if (TextUtils.isEmpty(packageNameToUse)) {
            return;
        }
        serviceBound = CustomTabsClient.bindCustomTabsService(getContext(), packageNameToUse, connection);
        boolean correctIntentFilter = contextIntentFilterMatchesRedirectUri(getContext());
        customTabsSupported = serviceBound && correctIntentFilter;
        browserType = customTabsSupported ? BrowserType.CHROME_CUSTOM_TAB : BrowserType.WEB_VIEW;
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (serviceBound) {
            getContext().unbindService(connection);
            serviceBound = false;
        }
        session = null;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Intent intent = getActivity().getIntent();
        if (ConnectSdk.hasErrorRedirectUrlCall(intent)) {
            ConnectSdk.setRandomLogSessionId();
        }

        boolean ongoingAuth = intent != null && ConnectSdk.hasValidRedirectUrlCall(intent);
        boolean enableButton = !ongoingAuth;
        setEnabled(enableButton);
    }
}
