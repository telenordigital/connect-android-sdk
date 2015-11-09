package com.telenor.connect.externalbrowserexample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;

public class CodeProcessingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_processing);

        Intent intent = getIntent();
        if (intent.getDataString() != null && !intent.getDataString().isEmpty()) {
            Uri redirectUri = Uri.parse(intent.getDataString());
            if (redirectUri.getQueryParameter("code") != null) {
                ConnectSdk.getAccessTokenFromCode(redirectUri.getQueryParameter("code"),
                        new ConnectCallback() {
                    @Override
                    public void onSuccess(Object successData) {
                        Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Object errorData) {
                        // TODO Handle all oauth/token server errors here.
                    }
                });
            }
        }
    }
}
