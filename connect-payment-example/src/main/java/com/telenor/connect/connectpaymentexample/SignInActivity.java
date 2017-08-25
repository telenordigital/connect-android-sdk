package com.telenor.connect.connectpaymentexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.telenor.connect.ui.ConnectWebViewLoginButton;

public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ConnectWebViewLoginButton loginButton = (ConnectWebViewLoginButton) findViewById(R.id.login_button);
        loginButton.setLoginScopeTokens("payment.transactions.read payment.transactions.write");
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
