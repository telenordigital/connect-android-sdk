package com.telenor.connect.connectidexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ui.ConnectLoginButton;

public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ConnectLoginButton loginButton = (ConnectLoginButton) findViewById(R.id.login_button);
        loginButton.setLoginScopeTokens("profile openid");

        Switch forcedHeSwitch = (Switch) findViewById(R.id.switchForcedHe);

        forcedHeSwitch
                .setOnClickListener(new Switch.OnClickListener() {
                    private boolean state = false;
                    public void onClick(View v) {
                        ConnectSdk.setForcedHeEnabled(((Switch)v).isChecked());
                    }
                });

        ConnectSdk.setForcedHeEnabled(forcedHeSwitch.isEnabled());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
