package com.telenor.connect.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.R;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;

public class ConnectLoginButton extends ConnectButton {
    private static ArrayList<String> sLoginScopeTokens;

    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        setOnClickListener(new LoginClickListener());
    }

    public void setLoginScopeTokens(String... scopeTokens) {
        sLoginScopeTokens = new ArrayList<>(Arrays.asList(scopeTokens));
    }

    public void setLoginScopeTokens(ArrayList<String> scopeTokens) {
        sLoginScopeTokens = scopeTokens;
    }

    public ArrayList<String> getLoginScopeTokens() {
        return sLoginScopeTokens;
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Validator.SdkInitialized();

            ConnectIdService.getInstance().startConnectAuthentication(getActivity(),
                    getLoginScopeTokens());
        }
    }
}
