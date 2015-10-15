package com.telenor.ui;

import android.app.Activity;
import android.content.Intent;

import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.tests.R;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.ui.ConnectLoginButton;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.tests.TestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class ConnectLoginButtonTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        Whitebox.setInternalState(ConnectSdk.class, "sSdkInitialized", false);
    }

    @Test
    public void testLoginButtonClickWithoutInitializingSdk() {
        thrown.expect(ConnectNotInitializedException.class);
        Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.performClick();
    }

    @Test
    public void testLoginButtonClickWithInitializedSdk() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        Intent expected = new Intent(activity, ConnectActivity.class);

        Intent startedIntent = shadowOf(activity).peekNextStartedActivityForResult().intent;
        assertThat(startedIntent.getComponent(), is(expected.getComponent()));
        assertThat(startedIntent.getAction(), is(ConnectUtils.LOGIN_ACTION));
    }
}
