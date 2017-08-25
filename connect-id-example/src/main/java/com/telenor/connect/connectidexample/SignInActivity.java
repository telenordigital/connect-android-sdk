package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ui.ConnectLoginButton;
import com.telenor.connect.utils.ConnectUtils;

public class SignInActivity extends Activity {

    private View progressBar;
    private ConnectLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);

        loginButton.setLoginScopeTokens("profile openid");
        final View.OnClickListener buttonClickListener = loginButton.getOnClickListener();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                buttonClickListener.onClick(v);
            }
        });

        ConnectSdk.handleRedirectUriCallIfPresent(getIntent(), new ConnectCallback() {
            @Override
            public void onSuccess(Object successData) {
                goToSignedInActivity();
            }

            @Override
            public void onError(Object errorData) {
                Log.e(ConnectUtils.LOG_TAG, errorData.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectSdk.hasValidRedirectUrlCall(getIntent())) {
            showLoading();
        } else {
            showEnabledButton();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
    }

    private void showEnabledButton() {
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
    }

    private void goToSignedInActivity() {
        final Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Fallback if no intent-filter is set on the activity, or device does not suport Chrome Custom
    // Tabs, WebView will be used instead.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            goToSignedInActivity();
        }
    }
}
