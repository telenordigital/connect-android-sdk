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

    private static final String AUTH_FRAGMENT_TAG = "AUTH_FRAGMENT_TAG";
    private static final String FRAGMENT_VISIBLE = "FRAGMENT_VISIBLE";
    private Button loginButton;
    private View connectView;
    private Map<String, String> parameters;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final boolean fragmentVisible
                = connectView != null && connectView.getVisibility() == View.VISIBLE;
        outState.putBoolean(FRAGMENT_VISIBLE, fragmentVisible);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        parameters = new HashMap<>();
        parameters.put("scope", "openid profile");

        connectView = findViewById(R.id.connect_placeholder);
        Fragment authFragment = getSupportFragmentManager().findFragmentByTag("AUTH_FRAGMENT_TAG");

        if (authFragment == null) {
            authFragment = ConnectSdk.getAuthFragment(parameters);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.connect_placeholder, authFragment, AUTH_FRAGMENT_TAG)
                    .commit();
        }

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectView.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.GONE);
            }
        });

        boolean fragmentVisible = savedInstanceState != null
                && savedInstanceState.getBoolean(FRAGMENT_VISIBLE, false);

        if (fragmentVisible) {
            connectView.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        }
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
