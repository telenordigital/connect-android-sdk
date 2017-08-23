package com.telenor.connect.connectpaymentexample;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.AccessTokenCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        View payButton = findViewById(R.id.pay_button);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateTransactionTask(CartActivity.this).execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(getApplicationContext(), PaymentFinishedActivity.class);
        intent.putExtra(PaymentFinishedActivity.PAYMENT_RESULT_STRING, resultCode);
        startActivity(intent);
        finish();
    }

    static class CreateTransactionTask extends AsyncTask<Void, Void, JSONObject> {
        private Activity activity = null;

        CreateTransactionTask(Activity activity) {
            if (activity == null) {
                throw new NullPointerException();
            }

            this.activity = activity;
        }

        @Override
        protected JSONObject doInBackground(Void... unused) {
            try {
                HttpGet request = new HttpGet(activity.getResources()
                        .getString(R.string.resource_server_transaction_endpoint));
                request.setHeader("Authorization", "Bearer " + ConnectSdk.getAccessToken());
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(request);

                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONObject object = new JSONObject(data);

                    return object;
                } else if (status == 401) {
                    ConnectSdk.updateTokens(new AccessTokenCallback() {
                        @Override
                        public void onSuccess(String accessToken) {
                            new CreateTransactionTask(activity).execute();
                        }

                        @Override
                        public void onError(Object errorData) {
                            // TODO Add handling for not being able to get a new access token.
                            // For example by starting the authentication flow again when an
                            // "invalid_grant" error gets returned.
                        }
                    });
                } else {
                    // TODO Add handling for other HTTP error statuses.
                }
            } catch (IOException | JSONException e) {
                // TODO Handle IO and JSON exceptions.
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject jsonObject) {
            if (jsonObject != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConnectSdk.initializePayment(
                                    activity,
                                    jsonObject.getString("PAYMENT_LINK"));
                        } catch (JSONException e) {
                            // TODO Handle JSON exception.
                        }
                    }
                });
            }
        }
    }
}
