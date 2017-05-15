package com.telenor.mobileconnect.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.telenor.TestHelper;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.SdkProfile;
import com.telenor.connect.TestActivity;
import com.telenor.connect.tests.R;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.ui.ConnectLoginButton;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.RestHelper;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowTelephonyManager;

import static com.telenor.TestHelper.A_FAILING_WELL_KNOWN_API;
import static com.telenor.TestHelper.A_VALID_WELL_KNOWN_API;
import static com.telenor.TestHelper.BooleanSupplier;
import static com.telenor.TestHelper.MOCKED_WELL_KNOWN_ENDPONT;
import static com.telenor.TestHelper.WELL_KNOWN_API_MAP;
import static com.telenor.TestHelper.flushForegroundTasksUntilCallerIsSatisifed;
import static com.telenor.mobileconnect.MobileConnectTestHelper.A_FAILING_OPERATOR_DISCOVERY_API;
import static com.telenor.mobileconnect.MobileConnectTestHelper.A_VALID_OPERATOR_DISCOVERY_API;
import static com.telenor.mobileconnect.MobileConnectTestHelper.CustomRobolectricTestRunner;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_OD_ENDPONT;
import static com.telenor.mobileconnect.MobileConnectTestHelper.OPERATOR_DISCOVERY_API_MAP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.newInstanceOf;

@RunWith(CustomRobolectricTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*",
        "com.telenor.connect.ui.*", "com.telenor.connect.ConnectSdk",
        "com.telenor.mobileconnect.*", "com.telenor.TestHelper",
        "com.telenor.connect.WellKnownAPI",
        "class com.telenor.mobileconnect.ui.MobileConnectLoginButtonTest$1"})
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class MobileConnectLoginButtonTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Before
    public void before() throws Exception {
        Whitebox.setInternalState(ConnectSdk.class, "sdkProfile", (SdkProfile) null);
        Whitebox.setInternalState(RestHelper.class, "wellKnownApiMap", WELL_KNOWN_API_MAP);
        Whitebox.setInternalState(RestHelper.class, "operatorDiscoveryApiMap", OPERATOR_DISCOVERY_API_MAP);

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
        WELL_KNOWN_API_MAP.put(MOCKED_WELL_KNOWN_ENDPONT, A_FAILING_WELL_KNOWN_API);
        OPERATOR_DISCOVERY_API_MAP.put(MOCKED_OD_ENDPONT, A_FAILING_OPERATOR_DISCOVERY_API);

        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        final ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Activity start should fail, but it didn't!",
                flushForegroundTasksUntilCallerIsSatisifed(
                        1000,
                        new BooleanSupplier() {
                            @Override
                            public boolean getAsBoolean() {
                                return shadowOf(activity).peekNextStartedActivityForResult() != null;                            }
                        }
                ), is(false));
    }

    @Test
    public void clickingLoginButtonWithValidOperatorDiscoveryResultStartsConnectActivity() {
        WELL_KNOWN_API_MAP.put(MOCKED_WELL_KNOWN_ENDPONT, A_VALID_WELL_KNOWN_API);
        OPERATOR_DISCOVERY_API_MAP.put(MOCKED_OD_ENDPONT, A_VALID_OPERATOR_DISCOVERY_API);

        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        final ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Activity start fails!",
                flushForegroundTasksUntilCallerIsSatisifed(
                        3000,
                        new BooleanSupplier() {
                            @Override
                            public boolean getAsBoolean() {
                                return shadowOf(activity).peekNextStartedActivityForResult() != null;                            }
                        }
                ), is(true));

        assertThat(ConnectSdk.getWellKnownConfig().getIssuer(), is(TestHelper.DUMMY_ISSUER));
        Intent expected = new Intent(activity, ConnectActivity.class);
        Intent startedIntent = shadowOf(activity).peekNextStartedActivityForResult().intent;
        assertThat(startedIntent.getComponent(), is(expected.getComponent()));
        assertThat(startedIntent.getAction(), is(ConnectUtils.LOGIN_ACTION));
    }

    private static class PeekNextStarted implements BooleanSupplier {
        private Activity activity;

        @Override
        public boolean getAsBoolean() {
            return false;
        }
    }

}
