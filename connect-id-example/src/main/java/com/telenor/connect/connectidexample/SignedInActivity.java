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
import com.telenor.connect.id.IdToken;
import com.telenor.connect.ui.ConnectLoginButton;

import java.util.HashMap;

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

        updateSubjectFieldFromIdToken();

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

        ConnectLoginButton loginAgain = findViewById(R.id.login_again_button);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("prompt", "login");
        loginAgain.addLoginParameters(parameters);
        loginAgain.setLoginScopeTokens("profile openid");

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectSdk.updateTokens(new AccessTokenCallback() {
                    @Override
                    public void onSuccess(String accessToken) {
                        updateSubjectFieldFromIdToken();
                        Toast.makeText(SignedInActivity.this, "Successfully refreshed tokens", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Object errorData) {
                        Toast.makeText(SignedInActivity.this, "Failed to refresh tokens", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateSubjectFieldFromIdToken() {
        TextView userId = findViewById(R.id.user_id);
        IdToken idToken = ConnectSdk.getIdToken();
        if (idToken != null) {
            userId.setText(idToken.getSubject());
        } else {
            userId.setText("No id token found");
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToSignedInActivity() {
        final Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Fallback if no intent-filter is set on the activity, or device does not suport Chrome Custom
    // Tabs, WebView will be used instead.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            goToSignedInActivity();
        }
    }

}
