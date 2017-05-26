package com.telenor;

import com.telenor.connect.WellKnownAPI;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;

import static com.telenor.connect.WellKnownAPI.WellKnownConfig;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestHelper {

    public static final String DUMMY_ISSUER = "https://dummy/oauth";
    public static final Map<String, WellKnownAPI> WELL_KNOWN_API_MAP = new HashMap<>();
    public static final String
            MOCKED_WELL_KNOWN_ENDPONT = DUMMY_ISSUER + "/.well-known/openid-configuration";

    private static final long SLEEPING_TIME_IN_MILLIES = 10;

    public interface BooleanSupplier {
        boolean getAsBoolean();
    }

    public static boolean flushForegroundTasksUntilCallerIsSatisifed(
            long maxMillies,
            BooleanSupplier isCallerSatisfied) {
        while (maxMillies > 0) {
            if (isCallerSatisfied.getAsBoolean()) {
                return true;
            }
            maxMillies -= SLEEPING_TIME_IN_MILLIES;
            Robolectric.flushForegroundThreadScheduler();
            try {
                Thread.sleep(SLEEPING_TIME_IN_MILLIES);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted!", e);
            }
        }
        return false;
    }

    public static final WellKnownAPI MOCKED_FAILING_WELL_KNOWN_API = getFailingWellKnownApiMock();

    public static final WellKnownAPI MOCKED_VALID_WELL_KNOWN_API = getValidWellKnownApiMock();

    private static WellKnownAPI getValidWellKnownApiMock() {
        final WellKnownConfig wellKnownConfig = mock(WellKnownConfig.class);
        when(wellKnownConfig.getIssuer()).thenReturn(DUMMY_ISSUER);

        WellKnownAPI api = mock(WellKnownAPI.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<WellKnownConfig> callback =
                        (Callback<WellKnownConfig>) invocation.getArguments()[0];
                callback.success(wellKnownConfig, null);
                return null;
            }
        }).when(api).getWellKnownConfig(
                any(Callback.class));
        return api;

    }
    private static WellKnownAPI getFailingWellKnownApiMock() {
        WellKnownAPI api = mock(WellKnownAPI.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<WellKnownAPI.WellKnownConfig> callback =
                        (Callback<WellKnownAPI.WellKnownConfig>) invocation.getArguments()[0];
                callback.failure(null);
                return null;
            }
        }).when(api).getWellKnownConfig(any(Callback.class));
        return api;
    }


}
