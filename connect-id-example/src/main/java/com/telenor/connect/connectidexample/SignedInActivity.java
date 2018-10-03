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
import com.telenor.connect.id.ConnectTokensTO;

import retrofit2.Call;
import retrofit2.Response;

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
                goToLogin();
            }
        });

        TextView userId = findViewById(R.id.user_id);
        userId.setText(ConnectSdk.getIdToken().getSubject());
    }

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Example usage of ConnectSdk.getValidAccessToken
        ConnectSdk.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void success(String accessToken) {
                Toast.makeText(SignedInActivity.this,
                        "accessToken can now be used to access user sensitive resources",
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void unsuccessfulResult(Response response, boolean userDataRemoved) {
                // a 4xx response will sign out any signed in user
                if (userDataRemoved) {
                    goToLogin();
                }
                String text = response.body() != null
                        ? response.body().toString()
                        : "<empty response body>";
                Toast.makeText(SignedInActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(Call<ConnectTokensTO> call, Throwable error) {
                Toast.makeText(SignedInActivity.this,
                        "Failed to update token. Check connectivity and try again.",
                        Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void noSignedInUser() {
                goToLogin();
            }
        });
    }
}
