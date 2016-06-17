package com.telenor.connect.connectidexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectTokensStateTracker;
import com.telenor.connect.ui.ConnectButton;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends FragmentActivity {

    private ConnectButton loginButton;
    private View webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "openid profile");
        ConnectSdk.preLoadAuthFlow(
                parameters,
                getSupportFragmentManager(),
                R.id.web_view_placeholder);

        webView = findViewById(R.id.web_view_placeholder);
        loginButton = (ConnectButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.GONE);
            }
        });

        new ConnectTokensStateTracker() {
            @Override
            protected void onTokenStateChanged(boolean hasTokens) {
                if (hasTokens) {
                    Intent intent = new Intent(SignInActivity.this, SignedInActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        webView.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
    }
}
