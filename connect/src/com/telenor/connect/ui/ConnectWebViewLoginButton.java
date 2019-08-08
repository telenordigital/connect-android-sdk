package com.telenor.connect.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.headerenrichment.DismissDialogCallback;
import com.telenor.connect.headerenrichment.ShowLoadingCallback;
import com.telenor.connect.id.Claims;
import com.telenor.connect.id.IdProvider;
import com.telenor.connect.utils.ClaimsParameterFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class ConnectWebViewLoginButton extends ConnectButton implements AuthenticationButton {

    public static final int NO_CUSTOM_LAYOUT = -1;

    private ArrayList<String> acrValues;
    private Map<String, String> loginParameters;
    private ArrayList<String> loginScopeTokens;
    private int requestCode = 0xa987;
    private Claims claims;
    private int customLoadingLayout = NO_CUSTOM_LAYOUT;
    private ShowLoadingCallback showLoadingCallback;
    private DismissDialogCallback dismissDialogCallback;
    private OnClickListener onClickListener;

    public ConnectWebViewLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        IdProvider brand = ConnectSdk.getIdProvider();
        setText(getResources().getString(R.string.com_telenor_connect_login_button_text, brand.getName()));
        onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        };
        setOnClickListener(onClickListener);
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

    public int getCustomLoadingLayout() {
        return customLoadingLayout;
    }

    @Override
    public OnClickListener getOnClickListener() {
        return onClickListener;
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

    public void setCustomLoadingLayout(int customLoadingLayout) {
        this.customLoadingLayout = customLoadingLayout;
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

    protected void authenticate() {
        ConnectSdk.authenticate(
                getActivity(),
                getParameters(),
                getCustomLoadingLayout(),
                getRequestCode(),
                getShowLoadingCallback(),
                getDismissDialogCallback()
        );
    }

    public ShowLoadingCallback getShowLoadingCallback() {
        return showLoadingCallback;
    }

    public void setShowLoadingCallback(ShowLoadingCallback showLoadingCallback) {
        this.showLoadingCallback = showLoadingCallback;
    }

    public void setDismissDialogCallback(DismissDialogCallback dismissDialogCallback) {
        this.dismissDialogCallback = dismissDialogCallback;
    }

    public DismissDialogCallback getDismissDialogCallback() {
        return dismissDialogCallback;
    }
}
