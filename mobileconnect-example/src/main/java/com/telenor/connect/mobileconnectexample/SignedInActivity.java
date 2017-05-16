package com.telenor.connect.mobileconnectexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.mobileconnectexample.R;
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
        userId.setText(ConnectSdk.getIdToken().getSubject());

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

        ListView listView = (ListView)
                findViewById(R.id.logview);
        ConnectSdk.setLogSourceForLogView(listView);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

}
