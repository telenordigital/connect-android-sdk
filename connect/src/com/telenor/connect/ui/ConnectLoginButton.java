package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
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
import com.telenor.connect.utils.Validator;

import java.lang.ref.WeakReference;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ConnectLoginButton extends ConnectWebViewLoginButton {

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
    private boolean launchCustomTabInNewTask = true;
    private boolean serviceBound = false;
    private BrowserType browserType;
    private CustomTabsSession session;

    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        onClickListener = new LoginClickListener();
        setOnClickListener(onClickListener);
    }

    public void setLaunchCustomTabInNewTask(boolean launchCustomTabInNewTask) {
        this.launchCustomTabInNewTask = launchCustomTabInNewTask;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    private Uri getAuthorizeUri() {
        final Map<String, String> parameters = getParameters();
        return ConnectUrlHelper.getAuthorizeUri(parameters, browserType);
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

    private void launchWebViewAuthentication() {
        startWebViewAuthentication();
    }

    private void launchChromeCustomTabAuthentication() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);
        CustomTabsIntent cctIntent = builder.build();
        Intent intent = cctIntent.intent;
        if (launchCustomTabInNewTask) {
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + getContext().getPackageName()));
        }
        Uri authorizeUri = getAuthorizeUri();
        cctIntent.launchUrl(getActivity(), authorizeUri);
    }

    private void setSession(CustomTabsSession session) {
        this.session = session;
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.sdkInitialized();
            ConnectSdk.beforeAuthentication();

            if (!customTabsSupported) {
                launchWebViewAuthentication();
            } else {
                launchChromeCustomTabAuthentication();
            }
        }

    }

    private static class WeakReferenceCustomTabsServiceConnection extends CustomTabsServiceConnection {

        private final WeakReference<ConnectLoginButton> weakButton;

        WeakReferenceCustomTabsServiceConnection(WeakReference<ConnectLoginButton> weakButton) {
            this.weakButton = weakButton;
        }

        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            final ConnectLoginButton connectLoginButton = weakButton.get();
            if (connectLoginButton == null) {
                return;
            }
            client.warmup(0);
            final CustomTabsSession session = client.newSession(new WeakReferenceCustomTabsCallback(weakButton));
            connectLoginButton.setSession(session);
            if (session != null) {
                session.mayLaunchUrl(PRE_FETCH_URL, null, null);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private static class WeakReferenceCustomTabsCallback extends CustomTabsCallback {

        private final WeakReference<ConnectLoginButton> weakButton;

        WeakReferenceCustomTabsCallback(WeakReference<ConnectLoginButton> weakButton) {
            this.weakButton = weakButton;
        }

        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            final ConnectLoginButton connectLoginButton = weakButton.get();
            if (connectLoginButton == null) {
                return;
            }
            switch (navigationEvent) {
                case CustomTabsCallback.TAB_HIDDEN:
                    connectLoginButton.setEnabled(true);
                    return;
                case CustomTabsCallback.TAB_SHOWN:
                    connectLoginButton.setEnabled(false);
                    return;
                default:
            }
        }
    }
}