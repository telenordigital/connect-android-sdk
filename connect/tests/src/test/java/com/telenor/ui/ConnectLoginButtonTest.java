package com.telenor.ui;

import android.app.Activity;

import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.tests.R;
import com.telenor.connect.ui.ConnectLoginButton;
import com.telenor.tests.TestActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "connect/tests/src/main/AndroidManifest.xml", sdk = 18)
public class ConnectLoginButtonTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLoginButtonWithoutInitializingSdk() {
        thrown.expect(ConnectNotInitializedException.class);
        Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.performClick();
    }
}
