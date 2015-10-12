package com.telenor.connect.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;

public class ConnectLoginButton extends ConnectButton {
    private static ArrayList<String> sAcrValues;
    private static ArrayList<String> sLoginScopeTokens;

    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        setOnClickListener(new LoginClickListener());
    }

    public ArrayList<String> getAcrValues() {
        return sAcrValues;
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

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.SdkInitialized();

            ConnectSdk.authenticate(getActivity(), getLoginScopeTokens(), getAcrValues(), null);
        }
    }
}
