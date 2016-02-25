package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.telenor.connect.ConnectSdk;
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

        Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectSdk.logout();
            }
        });

        TextView userId = (TextView) findViewById(R.id.user_id);
        userId.setText(ConnectSdk.getSubjectId());

        if (ConnectSdk.getAccessToken() == null) {
            goToLogin();
        }
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
