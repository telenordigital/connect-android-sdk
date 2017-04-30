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

    private static final int READ_PHONE_STATE_REQUEST_CODE = 0x2322;

    public MobileConnectLoginButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_mobile_connect_login_button_text);
        checkOrRequestReadPhoneStatePermission();
    }

    private void checkOrRequestReadPhoneStatePermission() {
        Activity activity = getActivity();
        if (activity.checkCallingOrSelfPermission(READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_CODE);
            }
        }
    }
}
