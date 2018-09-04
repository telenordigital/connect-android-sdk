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

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectSdk.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                String jwt = ConnectSdk.getIdToken().getSerializedSignedJwt();
                String subject = ConnectSdk.getIdToken().getSubject();
                Toast.makeText(getApplicationContext(), "jwt returned \"" + jwt + "\" ", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "subject returned \"" + subject + "\" ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Object errorData) {
                Toast.makeText(getApplicationContext(), "onResume error occured ", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
