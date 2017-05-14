package com.telenor.connect.ui;

import android.app.Activity;
import android.content.Intent;

import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ConnectSdkProfile;
import com.telenor.connect.SdkProfile;
import com.telenor.connect.TestActivity;
import com.telenor.utils.TestUtils;
import com.telenor.connect.tests.R;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.RestHelper;

import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void before() {
        Whitebox.setInternalState(ConnectSdk.class, "sdkProfile", (SdkProfile) null);
    }

    @Test(expected = ConnectNotInitializedException.class)
    public void clickingLoginButtonBeforeInitializingSdkThrows() {
        Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();

        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.performClick();
    }

    @Test
    public void clickingLoginButtonWithInitializedSdkStartsConnectActivity() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        ConnectSdkProfile profile = (ConnectSdkProfile) ConnectSdk.getSdkProfile();
        Whitebox.setInternalState(
                RestHelper.class,
                "wellKnownApiMap",
                TestUtils.grabMap(profile.getWellKnownEndpoint(), TestUtils.WELL_KNOWN_API_THAT_FAILS));
        ;
        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Waiting for activity start failed!",
                TestUtils.flushForegroundTasksUntilCallerIsSatisifed(5000, new TestUtils.BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() {
                        return shadowOf(activity).peekNextStartedActivityForResult() != null;
                    }
                }), is(true));

        Intent expected = new Intent(activity, ConnectActivity.class);
        Intent startedIntent = shadowOf(activity).peekNextStartedActivityForResult().intent;
        assertThat(startedIntent.getComponent(), is(expected.getComponent()));
        assertThat(startedIntent.getAction(), is(ConnectUtils.LOGIN_ACTION));
    }
}
