package com.telenor.connect.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.headerenrichment.ShowLoadingCallback;
import com.telenor.connect.id.Claims;

import java.util.ArrayList;
import java.util.Map;

public class ConnectLoginButton extends RelativeLayout implements AuthenticationButton {

    private ConnectCustomTabLoginButton loginButton;
    private ProgressBar progressBar;
    private View.OnClickListener loginClickListener;

    public ConnectLoginButton(Context context) {
        super(context);
        init();
    }

    private void init() {
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
                loginClickListener.onClick(v);
            }
        });
        if (ConnectSdk.isDoInstantVerificationOnButtonInitialize()) {
            ConnectSdk.runInstantVerification();
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? VISIBLE : INVISIBLE);
        loginButton.setEnabled(!loading);
    }

    public ConnectLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConnectLoginButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
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
