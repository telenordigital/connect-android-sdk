package com.telenor.tests;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.telenor.connect.tests.R;

public class TestActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_telenor_test_activity_layout);
    }
}
