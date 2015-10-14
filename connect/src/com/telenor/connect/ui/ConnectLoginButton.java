package com.telenor.connect.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConnectLoginButton extends ConnectButton {
    private static ArrayList<String> sAcrValues;
    private static Map<String, String> sLoginParameters;
    private static ArrayList<String> sLoginScopeTokens;

    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        setOnClickListener(new LoginClickListener());
    }

    public ArrayList<String> getAcrValues() {
        return sAcrValues;
    }

    public Map<String, String> getLoginParameters() {
        return sLoginParameters;
    }

    public ArrayList<String> getLoginScopeTokens() {
        return sLoginScopeTokens;
    }

    public void setAcrValues(String... acrValues) {
        sAcrValues = new ArrayList<>(Arrays.asList(acrValues));
    }

    public void setAcrValues(ArrayList<String> acrValues) {
        sAcrValues = acrValues;
    }

    public void setLoginScopeTokens(String... scopeTokens) {
        sLoginScopeTokens = new ArrayList<>(Arrays.asList(scopeTokens));
    }

    public void setLoginScopeTokens(ArrayList<String> scopeTokens) {
        sLoginScopeTokens = scopeTokens;
    }

    public void setLoginParameters(Map<String, String> parameters) {
        sLoginParameters = parameters;
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.SdkInitialized();

            Map<String, String> parameters = new HashMap<>();
            if (getAcrValues() != null && !getAcrValues().isEmpty()) {
                parameters.put("acr_values", TextUtils.join(" ", getAcrValues()));
            }
            if (getLoginScopeTokens() != null && !getLoginScopeTokens().isEmpty()) {
                parameters.put("scope", TextUtils.join(" ", getLoginScopeTokens()));
            }

            if (getLoginParameters() != null && !getLoginParameters().isEmpty()){
                parameters.putAll(getLoginParameters());
            }

            ConnectSdk.authenticate(getActivity(), parameters);
        }
    }
}
