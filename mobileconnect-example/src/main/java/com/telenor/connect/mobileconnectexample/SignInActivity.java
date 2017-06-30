package com.telenor.connect.mobileconnectexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.telenor.mobileconnect.ui.MobileConnectLoginButton;

public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        MobileConnectLoginButton loginButton = (MobileConnectLoginButton) findViewById(R.id.login_button);
        loginButton.setLoginScopeTokens("openid phone");
        loginButton.setAcrValues("2");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.authorization_cancelled, extractError(data)),
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
        startActivity(intent);
        finish();
    }

    private static String extractError(Intent data) {
        if (data == null) {
            return null;
        }
        return data.getStringExtra("error");
    }
}
