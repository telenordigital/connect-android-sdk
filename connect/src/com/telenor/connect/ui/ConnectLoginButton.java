package com.telenor.connect.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;

public class ConnectLoginButton extends ConnectButton {
    public ConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_login_button_text);
        setOnClickListener(new LoginClickListener());
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            ConnectSdk.getConnectIdService().startConnectAuthentication(
                    ConnectLoginButton.this.getActivity());
        }
    }
}
