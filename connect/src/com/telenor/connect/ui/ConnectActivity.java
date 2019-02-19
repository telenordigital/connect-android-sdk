package com.telenor.connect.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.gson.Gson;
import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.R;
import com.telenor.connect.id.ConnectTokens;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ConnectActivity extends FragmentActivity implements ConnectCallback {

    private final Gson gson = new Gson();
    private Fragment singleFragment;
    private Set<BroadcastReceiver> receivers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_telenor_connect_activity_layout);

        FragmentManager manager = getSupportFragmentManager();
        final String fragmentTag = "SingleFragment";
        Fragment fragment = manager.findFragmentByTag(fragmentTag);

        Intent intent = getIntent();
        String action = intent.getAction();
        int loadingScreen = intent.getIntExtra(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA,
                R.layout.com_telenor_connect_default_loading_view);

        if (fragment == null) {
            fragment = new ConnectWebFragment();
            Bundle bundle = new Bundle(intent.getExtras());
            bundle.putString(ConnectUrlHelper.ACTION_ARGUMENT, action);
            bundle.putInt(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA, loadingScreen);
            fragment.setArguments(bundle);
            fragment.setRetainInstance(true);
            manager.beginTransaction()
                    .add(R.id.com_telenor_connect_fragment_container, fragment, fragmentTag)
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

    @Override
    public void onSuccess(Object successData) {
        if (ConnectSdk.isConfidentialClient()) {
            Map<String, String> authCodeData = (Map<String, String>) successData;
            Intent intent = new Intent();
            for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            ConnectTokens connectTokens = (ConnectTokens) successData;
            Intent data = new Intent();
            String json = gson.toJson(connectTokens);
            data.putExtra(ConnectSdk.EXTRA_CONNECT_TOKENS, json);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onError(Object errorData) {
        ConnectSdk.setRandomLogSessionId();
        Intent intent = new Intent();
        Map<String, String> authCodeData = (Map<String, String>) errorData;
        for (Map.Entry<String, String> entry : authCodeData.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        this.receivers.add(receiver);
        return super.registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Iterator<BroadcastReceiver> iterator = receivers.iterator(); iterator.hasNext();) {
            try {
                unregisterReceiver(iterator.next());
            } catch (IllegalArgumentException ignore) {}
            iterator.remove();
        }
    }
}
