package com.telenor.connect.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.telenor.connect.R;

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
            if (action.equals("LOGIN")) {
                fragment = new ConnectIdFragment();
            } else if (action.equals("PAYMENT")) {
                fragment = new ConnectPaymentFragment();
            }
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
