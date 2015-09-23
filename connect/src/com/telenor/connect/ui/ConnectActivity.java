package com.telenor.connect.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.utils.ConnectUtils;

public class ConnectActivity extends FragmentActivity {

    private static String FRAGMENT_TAG = "SingleFragment";
    private Fragment singleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_telenor_connect_activity_layout);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(FRAGMENT_TAG);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (fragment == null) {
            fragment = new ConnectWebFragment();
            Bundle b = new Bundle();
            b.putString(ConnectWebFragment.ACTION_ARGUMENT, action);
            if (action.equals(ConnectUtils.LOGIN_ACTION)) {
                if (intent.getStringArrayListExtra(ConnectUtils.LOGIN_SCOPE_TOKENS) == null ||
                        intent.getStringArrayListExtra(ConnectUtils.LOGIN_SCOPE_TOKENS).isEmpty()) {
                    throw new ConnectException("Cannot log in without scope tokens.");
                }
                b.putStringArrayList(ConnectUtils.LOGIN_SCOPE_TOKENS,
                        intent.getStringArrayListExtra(ConnectUtils.LOGIN_SCOPE_TOKENS));
            }
            if (action.equals(ConnectUtils.PAYMENT_ACTION)) {
                b.putString(ConnectWebFragment.URL_ARGUMENT,
                        intent.getStringExtra(ConnectSdk.EXTRA_PAYMENT_LOCATION));
            }
            fragment.setArguments(b);
            fragment.setRetainInstance(true);
            manager.beginTransaction()
                    .add(R.id.com_telenor_connect_fragment_container, fragment, FRAGMENT_TAG)
                    .commit();
        }

        singleFragment = fragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (singleFragment != null) {
            singleFragment.onConfigurationChanged(newConfig);
        }
    }
}
