package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectTokensStateTracker;
import com.telenor.connect.ui.ConnectLoginButton;
import com.telenor.connect.utils.Validator;

import java.util.HashMap;
import java.util.Map;

public class SignedInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        if (ConnectSdk.getAccessToken() == null) {
            goToLogin();
            return;
        }

        Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectSdk.logout();
            }
        });

        ConnectLoginButton topUpButton = (ConnectLoginButton) findViewById(R.id.top_up_button);
        topUpButton.setAcrValues("2");
        topUpButton.setLoginScopeTokens("profile", "email");

        Button topUpButton2 = (Button) findViewById(R.id.top_up_button2);
        topUpButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Validator.SdkInitialized();

                Map<String, String> parameters = new HashMap<>();
                parameters.put("acr_values", "2");
                parameters.put("scope", "profile email");

                ConnectSdk.authenticate(SignedInActivity.this, parameters, 0);
            }
        });

        new ConnectTokensStateTracker() {
            @Override
            protected void onTokenStateChanged(boolean hasTokens) {
                if (hasTokens == false) {
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
