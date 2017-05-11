package com.telenor.mobileconnect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;

import com.telenor.connect.R;
import com.telenor.connect.ui.ConnectLoginButton;

import static android.Manifest.permission.READ_PHONE_STATE;

public class MobileConnectLoginButton extends ConnectLoginButton {

    public MobileConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_mobile_connect_login_button_text);
    }
}
