package com.telenor.connect.connectpaymentexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class PaymentFinishedActivity extends AppCompatActivity {

    public static final String PAYMENT_RESULT_STRING =
            "com.telenor.connect.connectpaymentexample.PAYMENT_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_finished);

        Intent intent = getIntent();
        int paymentResult = intent.getIntExtra(PAYMENT_RESULT_STRING, 0);

        TextView textView = (TextView) findViewById(R.id.payment_result_textview);
        textView.setText(paymentResult == Activity.RESULT_OK
                ? "The payment was completed succesfully"
                : "The payment was not completed successfully");
    }
}
