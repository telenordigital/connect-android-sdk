package com.telenor.mobileconnect;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.tests.BuildConfig;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;

import org.junit.runners.model.InitializationError;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;

import static com.telenor.TestHelper.MOCKED_WELL_KNOWN_ENDPONT;
import static com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI.OperatorDiscoveryResult;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI.OperatorSelectionResult;

public class MobileConnectTestHelper {

    public static final String MOCKED_OD_ENDPONT = "https://fake/operator/discovery";
    public static final String MOCKED_MOBILE_CONNECT_CLIENT = "mobileconnect-client";
    public static final String MOCKED_MOBILE_CONNECT_SECRET = "mobi1ec0nnec1-5ecret";
    public static final String MOCKED_MOBILE_REDIRECT_URI = "http://localhost/redirect";
    public static final Map<String, OperatorDiscoveryAPI> OPERATOR_DISCOVERY_API_MAP = new HashMap<>();

    public static final OperatorDiscoveryAPI MOCKED_FAILING_OPERATOR_DISCOVERY_API =
            getFailingOperatorDiscoveryApiMock();
    public static final OperatorDiscoveryAPI MOCKED_VALID_OPERATOR_DISCOVERY_API =
            getValidOperatorDiscoveryApiMock();
    public static final OperatorDiscoveryAPI MOCKED_VALID_OPERATOR_SELECTION_API =
            getValidOperatorSelectionApiMock();

    private static final String MOCKED_API_URL = "https://dummy/operator/oauth";
    private static final String MOCKED_CLIENT_ID = "dummy-client";

    private static OperatorDiscoveryAPI getFailingOperatorDiscoveryApiMock() {
        OperatorDiscoveryAPI api = mock(OperatorDiscoveryAPI.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<OperatorDiscoveryResult> callback =
                        (Callback<OperatorDiscoveryResult>) invocation.getArguments()[4];
                callback.failure(null);
                return null;
            }
        }).when(api).getOperatorDiscoveryResult_ForMccMnc(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(Callback.class));
        return api;
    }

    private static OperatorDiscoveryAPI getValidOperatorSelectionApiMock() {
        final OperatorSelectionResult osResult =
                mock(OperatorSelectionResult.class);
        when(osResult.getEndpoint()).thenReturn(MOCKED_MOBILE_REDIRECT_URI);
        OperatorDiscoveryAPI api = getFailingOperatorDiscoveryApiMock();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<OperatorSelectionResult> callback =
                        (Callback<OperatorSelectionResult>) invocation.getArguments()[2];
                callback.success(osResult, null);
                return null;
            }
        }).when(api).getOperatorSelectionResult(
                anyString(),
                anyString(),
                any(Callback.class));
        return api;
    }

    private static OperatorDiscoveryAPI getValidOperatorDiscoveryApiMock() {
        final OperatorDiscoveryResult odResult =
                mock(OperatorDiscoveryResult.class);
        when(odResult.getWellKnownEndpoint()).thenReturn(MOCKED_WELL_KNOWN_ENDPONT);
        when(odResult.getMobileConnectApiUrl()).thenReturn(HttpUrl.parse(MOCKED_API_URL));
        when(odResult.getClientId()).thenReturn(MOCKED_CLIENT_ID);

        OperatorDiscoveryAPI api = mock(OperatorDiscoveryAPI.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<OperatorDiscoveryResult> callback =
                        (Callback<OperatorDiscoveryResult>) invocation.getArguments()[4];
                callback.success(odResult, null);
                return null;
            }
        }).when(api).getOperatorDiscoveryResult_ForMccMnc(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(Callback.class));
        return api;
    }

    /**
     * A custom Robolectric test runner that fixes the resources finding problem
     * when tests are to be executed in Android Studio w/Gradle.
     *
     * Should have no effect in all other cases.
     *
     * See: https://github.com/robolectric/robolectric/issues/1592
     */
    public static class CustomRobolectricTestRunner extends RobolectricTestRunner {
        public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
            if (System.getProperty("android.resources") != null) {
                return;
            }
            String pathToMerged = "build/intermediates/res/merged/" + BuildConfig.BUILD_TYPE;
            if (new File(pathToMerged).exists()) {
                System.setProperty("android.resources", pathToMerged);
            }
        }
    }
}
