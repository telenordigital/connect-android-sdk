package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
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

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectSdk.logout();
            }
        });

        findViewById(R.id.change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchChromeCustomTabChangePassword();
            }
        });

        TextView userId = findViewById(R.id.user_id);
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
    }

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchChromeCustomTabChangePassword() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        String url = "https://connect.staging.telenordigital.com" +
                "/gui/mypage/changepassword?mypage_back_link=mitttelenor://sso-credentials-changed";
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

}
