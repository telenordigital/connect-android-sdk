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
import com.telenor.connect.id.UserInfo;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

        ConnectSdk.getValidAccessToken(new AccessTokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                Toast.makeText(SignedInActivity.this, "Got valid access token: " + accessToken, Toast.LENGTH_LONG).show();
                ConnectSdk.getUserInfo(new Callback<UserInfo>() {
                    @Override
                    public void success(UserInfo userInfo, Response response) {
                        Toast.makeText(SignedInActivity.this, "Got userInfo: " + userInfo.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(SignedInActivity.this, "Got getUserInfo error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError (Object errorData){
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
