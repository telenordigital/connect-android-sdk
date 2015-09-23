package com.telenor.connect.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.telenor.connect.R;

public class ConnectPaymentButton extends ConnectButton {
    public ConnectPaymentButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(R.string.com_telenor_connect_payment_button_text);
    }
}
