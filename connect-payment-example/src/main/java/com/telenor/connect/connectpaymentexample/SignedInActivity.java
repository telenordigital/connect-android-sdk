package com.telenor.connect.connectpaymentexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectTokensStateTracker;

public class SignedInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        if (ConnectSdk.getAccessToken() == null) {
            goToLogin();
            return;
        }

        View logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectSdk.logout();
            }
        });

        View purchaseButton = findViewById(R.id.purchase_button);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                startActivity(intent);
            }
        });

        if (ConnectSdk.getAccessToken() == null) {
            goToLogin();
        }

        new ConnectTokensStateTracker() {
            @Override
            protected void onTokenStateChanged(boolean hasTokens) {
                if (!hasTokens) {
                    goToLogin();
                }
            }
        };

    }

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

}
