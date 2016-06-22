package com.telenor.connect.connectidexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends FragmentActivity implements ConnectCallback {

    private Button loginButton;
    private View connectView;
    private Map<String, String> parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        parameters = new HashMap<>();
        parameters.put("scope", "openid profile");

        connectView = findViewById(R.id.connect_placeholder);
        final Fragment authFragment = ConnectSdk.getAuthFragment(parameters);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.connect_placeholder, authFragment)
                .commit();
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectView.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (connectView.getVisibility() == View.VISIBLE) {
            connectView.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSuccess(Object successData) {
        Intent intent = new Intent(SignInActivity.this, SignedInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Object errorData) {
        connectView.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
        final Fragment authFragment = ConnectSdk.getAuthFragment(parameters);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.connect_placeholder, authFragment)
                .commit();
    }
}
