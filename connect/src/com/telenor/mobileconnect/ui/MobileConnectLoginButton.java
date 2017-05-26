package com.telenor.mobileconnect.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.telenor.connect.R;
import com.telenor.connect.ui.ConnectLoginButton;

public class MobileConnectLoginButton extends ConnectLoginButton {

    public MobileConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_mobile_connect_login_button_text);
    }
}
