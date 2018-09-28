package com.telenor.connect.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.id.Claims;
import com.telenor.connect.id.ConnectStore;
import com.telenor.connect.utils.ClaimsParameterFormatter;
import com.telenor.connect.utils.Validator;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConnectWebViewLoginButton extends ConnectButton {

    public static final int NO_CUSTOM_LAYOUT = -1;

    private ArrayList<String> acrValues;
    private Map<String, String> loginParameters;
    private ArrayList<String> loginScopeTokens;
    private int requestCode = 0xa987;
    private Claims claims;
    private int customLoadingLayout = NO_CUSTOM_LAYOUT;
    private ConnectStore connectStore;

    public ConnectWebViewLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        setOnClickListener(new LoginClickListener());
        connectStore = new ConnectStore(context);
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

    public void addLoginParameters(Map<String, String> parameters) {
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

    protected void startWebViewAuthentication() {
        ConnectSdk.authenticate(getActivity(), getParameters(), getCustomLoadingLayout(), getRequestCode());
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

        handlePromptAndLogSessionId(parameters);
        return parameters;
    }

    private void addClaims(Map<String, String> parameters) {
        if (getClaims() != null && getClaims().getClaimsAsSet() != null) {
            parameters.put("claims", ClaimsParameterFormatter.asJson(getClaims()));
        }
    }

    private void handlePromptAndLogSessionId(Map<String, String> parameters) {
        if (TextUtils.isEmpty(parameters.get("prompt")) && !ConnectSdk.isCellularDataNetworkConnected()) {
            parameters.put("prompt", "no_seam");
        }
        if (TextUtils.isEmpty(parameters.get("log_session_id"))) {
            parameters.put("log_session_id", ConnectSdk.getLogSessionId());
        }
    }


    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.sdkInitialized();
            ConnectSdk.beforeAuthentication();
            startWebViewAuthentication();
        }

    }
}
