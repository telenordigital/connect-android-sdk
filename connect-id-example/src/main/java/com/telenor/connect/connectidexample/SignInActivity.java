package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ui.ConnectLoginChromeTabButton;
import com.telenor.connect.utils.ConnectUtils;

public class SignInActivity extends Activity {

    private View progressBar;
    private ConnectLoginChromeTabButton loginButton;

    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectSdk.intentHasValidRedirectUrlCall(getIntent())) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            loginButton.preLoad();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        loginButton = (ConnectLoginChromeTabButton) findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);

        loginButton.setLoginScopeTokens("profile openid");
        final View.OnClickListener buttonClickListener = loginButton.getOnClickListener();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                loginButton.setEnabled(false);
                buttonClickListener.onClick(v);
            }
        });

        ConnectSdk.checkIntentForAndHandleRedirectUrlCall(getIntent(), new ConnectCallback() {
            @Override
            public void onSuccess(Object successData) {
                final Intent startIntent
                        = new Intent(getApplicationContext(), SignedInActivity.class);
                startIntent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startIntent);
                finish();
            }

            @Override
            public void onError(Object errorData) {
                Log.e(ConnectUtils.LOG_TAG, errorData.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
