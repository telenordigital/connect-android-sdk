package com.telenor.connect.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.id.Claims;
import com.telenor.connect.utils.ClaimsParameterFormatter;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.Validator;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConnectWebViewLoginButton extends ConnectButton {

    protected static final int NO_CUSTOM_LAYOUT = -1;

    private ArrayList<String> acrValues;
    private Map<String, String> loginParameters;
    private ArrayList<String> loginScopeTokens;
    private int requestCode = 0xa987;
    private Claims claims;
    private int customLoadingLayout = NO_CUSTOM_LAYOUT;

    public ConnectWebViewLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        setOnClickListener(new LoginClickListener());
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

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.sdkInitialized();

            Map<String, String> parameters = new HashMap<>();
            if (getAcrValues() != null && !getAcrValues().isEmpty()) {
                parameters.put(ConnectUtils.ACR_VALUES_PARAM_NAME,
                        TextUtils.join(" ", getAcrValues()));
            }

            if (getLoginScopeTokens() != null && !getLoginScopeTokens().isEmpty()) {
                parameters.put("scope", TextUtils.join(" ", getLoginScopeTokens()));
            }

            if (claims != null && claims.getClaimsAsSet() != null) {
                try {
                    parameters.put("claims", ClaimsParameterFormatter.asJson(claims));
                } catch (JSONException e) {
                    throw new ConnectException("Failed to create claims Json. claims="+claims, e);
                }
            }

            if (getLoginParameters() != null && !getLoginParameters().isEmpty()){
                parameters.putAll(getLoginParameters());
            }

            if (customLoadingLayout == NO_CUSTOM_LAYOUT) {
                ConnectSdk.authenticate(getActivity(), parameters, requestCode);
            } else {
                ConnectSdk.authenticate(getActivity(),
                        parameters,
                        customLoadingLayout,
                        requestCode);
            }
        }
    }
}
