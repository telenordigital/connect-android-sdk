package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.AccessTokenCallback;
import com.telenor.connect.id.ConnectTokensStateTracker;

public class SignedInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);
        if (ConnectSdk.getAccessToken() == null) {
            goToLogin();
            return;
        }

        testGetValidAccessToken();

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectSdk.logout();
            }
        });

        TextView userId = findViewById(R.id.user_id);
        userId.setText(ConnectSdk.getIdToken().getSubject());

        new ConnectTokensStateTracker() {
            @Override
            protected void onTokenStateChanged(boolean hasTokens) {
                if (!hasTokens) {
                    goToLogin();
                }
            }
        };
    }

    private void testGetValidAccessToken() {
        ConnectSdk.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                Toast.makeText(SignedInActivity.this, "Got valid access token: " + accessToken, Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ConnectSdk.fakeAccessTokenExpiration();
                testGetValidAccessToken();
            }

            @Override
            public void onError(Object errorData) {
                Toast.makeText(SignedInActivity.this, "Got getValidAccessToken error: " + errorData.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

}
