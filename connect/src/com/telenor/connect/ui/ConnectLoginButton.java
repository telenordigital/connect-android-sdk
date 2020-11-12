package com.telenor.connect.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.headerenrichment.DismissDialogCallback;
import com.telenor.connect.headerenrichment.HeLogic;
import com.telenor.connect.headerenrichment.ShowLoadingCallback;
import com.telenor.connect.id.Claims;
import com.telenor.connect.utils.TurnOnMobileDataDialogAnalytics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public class ConnectLoginButton extends RelativeLayout
        implements AuthenticationButton, TurnOnMobileDataDialogFragment.ContinueListener {

    private ConnectCustomTabLoginButton loginButton;
    private ProgressBar progressBar;
    private View.OnClickListener loginClickListener;

    public ConnectLoginButton(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        inflate(getContext(), R.layout.com_telenor_connect_login_button_with_progress_bar, this);
        progressBar = findViewById(R.id.com_telenor_connect_login_button_progress_bar);
        loginButton = findViewById(R.id.com_telenor_connect_login_button);
        loginButton.setShowLoadingCallback(new ShowLoadingCallback() {
            @Override
            public void stop() {
                setLoading(false);
            }
        });
        loginClickListener = loginButton.getOnClickListener();
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);

                boolean showTurnOnMobileDataDialog
                        = !mobileNetworkIsAvailable(context)
                        && ConnectSdk.isTurnOnMobileDataDialogEnabled();

                if (!showTurnOnMobileDataDialog || HeLogic.canNotDirectNetworkTraffic) {
                    loginClickListener.onClick(v);
                    return;
                }

                final TurnOnMobileDataDialogFragment turnOnMobileDataDialogFragment = new TurnOnMobileDataDialogFragment();
                loginButton.setDismissDialogCallback(new DismissDialogCallback() {
                    @Override
                    public void dismiss() {
                        turnOnMobileDataDialogFragment.dismiss();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public TurnOnMobileDataDialogAnalytics getAnalytics() {
                        return new TurnOnMobileDataDialogAnalytics(
                                ConnectSdk.isTurnOnMobileDataDialogEnabled(),
                                true,
                                turnOnMobileDataDialogFragment.isAtomaticButtonPressed(),
                                turnOnMobileDataDialogFragment.isManualButtonPressed()
                        );
                    }
                });
                FragmentManager fragmentManager = ((FragmentActivity) loginButton.getActivity()).getSupportFragmentManager();
                turnOnMobileDataDialogFragment.show(fragmentManager, "TurnOnMobileDataFragment");
                turnOnMobileDataDialogFragment.setContinueListener(ConnectLoginButton.this);
            }
        });
        if (ConnectSdk.isDoInstantVerificationOnButtonInitialize()) {
            ConnectSdk.runInstantVerification();
        }
    }

    /**
     * @deprecated Method is deprecated in Android and might be not safe to use. Fallbacks
     * to {@link HeLogic#isCellularDataNetworkConnected()} are present for failure cases.
     */
    @Deprecated
    private boolean mobileNetworkIsAvailable(Context context) {
        boolean mobileDataEnabled;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            Method method = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataEnabled = (Boolean)method.invoke(connectivityManager);
        } catch (NoSuchMethodException e) {
            // Use old way
            return HeLogic.isCellularDataNetworkConnected();
        } catch (SecurityException e) {
            // Use old way
            return HeLogic.isCellularDataNetworkConnected();
        } catch (Exception e) {
            return false;
        }
        return mobileDataEnabled;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? VISIBLE : INVISIBLE);
        loginButton.setEnabled(!loading);
    }

    @Override
    public void onContinueClicked(DialogFragment dialog) {
        loginClickListener.onClick(null);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setLoading(false);
    }

    public ConnectLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ConnectLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConnectLoginButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (loginButton == null || loginButton.getActivity() == null) {
            return;
        }
        Intent intent = loginButton.getActivity().getIntent();
        boolean ongoingAuth = intent != null && ConnectSdk.hasValidRedirectUrlCall(intent);
        setLoading(ongoingAuth);
    }

    @Override
    public ArrayList<String> getAcrValues() {
        return loginButton.getAcrValues();
    }

    @Override
    public Map<String, String> getLoginParameters() {
        return loginButton.getLoginParameters();
    }

    @Override
    public ArrayList<String> getLoginScopeTokens() {
        return loginButton.getLoginScopeTokens();
    }

    @Override
    public int getRequestCode() {
        return loginButton.getRequestCode();
    }

    @Override
    public Claims getClaims() {
        return loginButton.getClaims();
    }

    @Override
    public int getCustomLoadingLayout() {
        return loginButton.getCustomLoadingLayout();
    }

    @Override
    public OnClickListener getOnClickListener() {
        return loginButton.getOnClickListener();
    }

    @Override
    public void setAcrValues(String... acrValues) {
        loginButton.setAcrValues(acrValues);
    }

    @Override
    public void setAcrValues(ArrayList<String> acrValues) {
        loginButton.setAcrValues(acrValues);
    }

    @Override
    public void setLoginScopeTokens(String... scopeTokens) {
        loginButton.setLoginScopeTokens(scopeTokens);
    }

    @Override
    public void setLoginScopeTokens(ArrayList<String> scopeTokens) {
        loginButton.setLoginScopeTokens(scopeTokens);
    }

    @Override
    public void setExtraLoginParameters(Map<String, String> parameters) {
        loginButton.setExtraLoginParameters(parameters);
    }

    @Override
    public void setRequestCode(int requestCode) {
        loginButton.setRequestCode(requestCode);
    }

    @Override
    public void setClaims(Claims claims) {
        loginButton.setClaims(claims);
    }

    @Override
    public void setCustomLoadingLayout(int customLoadingLayout) {
        loginButton.setCustomLoadingLayout(customLoadingLayout);
    }
}
