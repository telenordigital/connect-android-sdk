package com.telenor.connect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.BrowserType;
import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.id.ConnectStore;
import com.telenor.connect.utils.ClaimsParameterFormatter;
import com.telenor.connect.utils.CustomTabsHelper;
import com.telenor.connect.utils.Validator;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ConnectLoginButton extends ConnectWebViewLoginButton {

    private static final Uri PRE_FETCH_URL
            = Uri.parse(
            ConnectSdk
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
    private BrowserType browserType;
    private ConnectStore connectStore;
    private CustomTabsSession session;

    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        connectStore = new ConnectStore(getContext());
        setText(R.string.com_telenor_connect_login_button_text);
        connection = new WeakReferenceCustomTabsServiceConnection(new WeakReference<>(this));
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
        return ConnectSdk.getAuthorizeUri(parameters, browserType);
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

        parameters.put("state", connectStore.generateSessionStateParam());
        parameters.put("log_session_id", ConnectSdk.getLogSessionId());

        if (!ConnectSdk.isCellularDataNetworkConnected()) {
            parameters.put("prompt", "no_seam");
        }

        if (getLoginParameters() != null && !getLoginParameters().isEmpty()) {
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
        return context.getPackageName().equals(componentName.getPackageName());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        boolean serviceBound = CustomTabsClient.bindCustomTabsService(
                getContext(), CustomTabsHelper.getPackageNameToUse(getContext()), connection);
        boolean correctIntentFilter = contextIntentFilterMatchesRedirectUri(getContext());
        customTabsSupported = serviceBound && correctIntentFilter;
        browserType = customTabsSupported ? BrowserType.CHROME_CUSTOM_TAB : BrowserType.WEB_VIEW;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (connection != null) {
            getContext().unbindService(connection);
            connection = null;
        }
    }

    private void launchWebViewAuthentication() {
        int customLoadingLayout = getCustomLoadingLayout();
        if (customLoadingLayout == NO_CUSTOM_LAYOUT) {
            ConnectSdk.authenticate(getActivity(), getParameters(), getRequestCode());
        } else {
            ConnectSdk.authenticate(
                    getActivity(), getParameters(), customLoadingLayout, getRequestCode());
        }
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