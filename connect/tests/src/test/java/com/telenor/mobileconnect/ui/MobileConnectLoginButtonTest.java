package com.telenor.mobileconnect.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.utils.TestUtils;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.TestActivity;
import com.telenor.connect.tests.R;
import com.telenor.connect.ui.ConnectLoginButton;
import com.telenor.connect.utils.RestHelper;
import com.telenor.mobileconnect.MobileConnectSdkProfile;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowTelephonyManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.newInstanceOf;

@RunWith(TestUtils.CustomRobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class MobileConnectLoginButtonTest {

    private static final String MOCKED_OD_ENDPONT = "https://fake.operator.discovery";

    @Before
    public void setUp() throws Exception {
        TelephonyManager manager = newInstanceOf(TelephonyManager.class);
        ShadowTelephonyManager shadowManager = shadowOf(manager);
        shadowManager.setNetworkOperator("00000");

        Application application = RuntimeEnvironment.application;
        ShadowContextImpl shadowContext = (ShadowContextImpl) shadowOf(application.getBaseContext());
        shadowContext.setSystemService(Context.TELEPHONY_SERVICE, manager);

        ConnectSdk.sdkInitializeMobileConnect(
                RuntimeEnvironment.application,
                OperatorDiscoveryConfig
                        .builder()
                        .endpoint(MOCKED_OD_ENDPONT)
                        .clientId("mobileconnect-client")
                        .clientSecret("secret")
                        .redirectUri("http://localhost/redirect")
                        .build());
    }

    @Test
    public void clickingLoginButtonWithFailedOperatorDiscoveryDoesNotStartConnectActivity() {
        Whitebox.setInternalState(
                RestHelper.class,
                "operatorDiscoveryApiMap",
                TestUtils.grabMap(MOCKED_OD_ENDPONT, TestUtils.A_FAILING_OPERATOR_DISCOVERY_API));
        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Waiting for activity start should fail!",
                TestUtils.flushForegroundTasksUntilCallerIsSatisifed(1000, new TestUtils.BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() {
                        return shadowOf(activity).peekNextStartedActivityForResult() != null;
                    }
                }), is(false));
    }

    @Test
    public void clickingLoginButtonWithValidOperatorDiscoveryResultStartsConnectActivity() {
        Whitebox.setInternalState(
                RestHelper.class,
                "operatorDiscoveryApiMap",
                TestUtils.grabMap(MOCKED_OD_ENDPONT, TestUtils.A_VALID_OPERATOR_DISCOVERY_API));
        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Waiting for activity start fails!",
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
