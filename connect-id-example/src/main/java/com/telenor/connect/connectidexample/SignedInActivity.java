package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.AccessTokenCallback;
import com.telenor.connect.id.ConnectTokensStateTracker;
import com.telenor.connect.utils.ConnectUtils;

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
        userId.setText(ConnectSdk.getIdToken().getAuthenticationUsername());

        ConnectSdk.updateTokens(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                Toast.makeText(SignedInActivity.this, "We have successfully updated tokens. accessToken=" + accessToken, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Object errorData) {
                Toast.makeText(SignedInActivity.this, "Failed to update tokens", Toast.LENGTH_SHORT).show();
                Log.e(ConnectUtils.LOG_TAG, "error: " + errorData.toString());
            }
        });

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

}
