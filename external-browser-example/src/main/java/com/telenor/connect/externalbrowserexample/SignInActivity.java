package com.telenor.connect.externalbrowserexample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ui.ConnectButton;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ConnectButton loginButton = (ConnectButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> signinParameters = new HashMap<>();
                signinParameters.put("scope", "profile");

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(ConnectSdk.getAuthorizeUri(signinParameters).toString()));
                startActivity(intent);
            }
        });
    }
}
