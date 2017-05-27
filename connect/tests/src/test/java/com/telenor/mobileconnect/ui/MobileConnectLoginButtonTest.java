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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowTelephonyManager;

import static com.telenor.TestHelper.MOCKED_FAILING_WELL_KNOWN_API;
import static com.telenor.TestHelper.MOCKED_VALID_WELL_KNOWN_API;
import static com.telenor.TestHelper.BooleanSupplier;
import static com.telenor.TestHelper.MOCKED_WELL_KNOWN_ENDPONT;
import static com.telenor.TestHelper.WELL_KNOWN_API_MAP;
import static com.telenor.TestHelper.flushForegroundTasksUntilCallerIsSatisifed;
import static com.telenor.mobileconnect.MobileConnectTestHelper.CustomRobolectricTestRunner;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_FAILING_OPERATOR_DISCOVERY_API;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_MOBILE_CONNECT_CLIENT;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_MOBILE_CONNECT_SECRET;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_MOBILE_REDIRECT_URI;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_OD_ENDPONT;
import static com.telenor.mobileconnect.MobileConnectTestHelper.MOCKED_VALID_OPERATOR_DISCOVERY_API;
import static com.telenor.mobileconnect.MobileConnectTestHelper.OPERATOR_DISCOVERY_API_MAP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.newInstanceOf;

/**
 * Since it's as good as impossible to run PowerMock
 * rule here (it sucks with anonymous classes and the
 * ignore list is bound to become very long), we mock
 * REST APIs by manipulating the API cache implemented
 * in RestHelper class.
 */

@RunWith(CustomRobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class MobileConnectLoginButtonTest {

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
                        .clientId(MOCKED_MOBILE_CONNECT_CLIENT)
                        .clientSecret(MOCKED_MOBILE_CONNECT_SECRET)
                        .redirectUri(MOCKED_MOBILE_REDIRECT_URI)
                        .build());
    }

    @Test
    public void clickingLoginButtonWithFailingOperatorDiscoveryDoesNotStartConnectActivity() {
        WELL_KNOWN_API_MAP.put(MOCKED_WELL_KNOWN_ENDPONT, MOCKED_FAILING_WELL_KNOWN_API);
        OPERATOR_DISCOVERY_API_MAP.put(MOCKED_OD_ENDPONT, MOCKED_FAILING_OPERATOR_DISCOVERY_API);

        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        final ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Activity is started",
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
    public void clickingLoginButtonWithValidOperatorDiscoveryResultStartsConnectActivityAndReturnsValidWellKnownConfig() {
        WELL_KNOWN_API_MAP.put(MOCKED_WELL_KNOWN_ENDPONT, MOCKED_VALID_WELL_KNOWN_API);
        OPERATOR_DISCOVERY_API_MAP.put(MOCKED_OD_ENDPONT, MOCKED_VALID_OPERATOR_DISCOVERY_API);

        final Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        final ConnectLoginButton button = (ConnectLoginButton) activity.findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
        button.performClick();

        assertThat("Activity is started",
                flushForegroundTasksUntilCallerIsSatisifed(
                        3000,
                        new BooleanSupplier() {
                            @Override
                            public boolean getAsBoolean() {
                                return shadowOf(activity).peekNextStartedActivityForResult() != null;                            }
                        }
                ), is(true));
        Intent expected = new Intent(activity, ConnectActivity.class);
        Intent startedIntent = shadowOf(activity).peekNextStartedActivityForResult().intent;
        assertThat(startedIntent.getComponent(), is(expected.getComponent()));
        assertThat(startedIntent.getAction(), is(ConnectUtils.LOGIN_ACTION));
        assertThat(ConnectSdk.getWellKnownConfig().getIssuer(), is(TestHelper.DUMMY_ISSUER));
    }
}
