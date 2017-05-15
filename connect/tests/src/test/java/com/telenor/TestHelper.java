package com.telenor;

import com.google.gson.Gson;
import com.telenor.connect.WellKnownAPI;
import com.telenor.connect.tests.BuildConfig;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;

import org.junit.runners.model.InitializationError;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;

public class TestHelper {

    public static final String DUMMY_ISSUER = "https://dummy/oauth";
    public  static final Map<String, WellKnownAPI> WELL_KNOWN_API_MAP = new HashMap<>();
    public  static final String MOCKED_WELL_KNOWN_ENDPONT =
            DUMMY_ISSUER + "/.well-known/openid-configuration";

    private static final long SLEEPING_TIME_IN_MILLIES = 10;

    public interface BooleanSupplier {
        boolean getAsBoolean();
    }

    public static boolean flushForegroundTasksUntilCallerIsSatisifed(
            long maxMillies,
            BooleanSupplier isCallerSatisfied) {
        for (; maxMillies > 0; maxMillies -= SLEEPING_TIME_IN_MILLIES) {
            if (isCallerSatisfied.getAsBoolean()) {
                return true;
            }
            Robolectric.flushForegroundThreadScheduler();
            try {
                Thread.sleep(SLEEPING_TIME_IN_MILLIES);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted!", e);
            }
        }
        return false;
    }

    public static final WellKnownAPI A_FAILING_WELL_KNOWN_API =
            new WellKnownAPI() {
                @Override
                public void getWellKnownConfig(Callback<WellKnownConfig> callback) {
                    callback.failure(null);
                }
            };

    public static final WellKnownAPI A_VALID_WELL_KNOWN_API =
            new WellKnownAPI() {
                @Override
                public void getWellKnownConfig(Callback<WellKnownConfig> callback) {
                    Gson gson = new Gson();
                    WellKnownAPI.WellKnownConfig wnc = gson.fromJson(
                            A_VALID_WELL_KNOWN_BODY,
                            WellKnownAPI.WellKnownConfig.class);
                    callback.success(wnc, null);
                }
            };

    private static String A_VALID_WELL_KNOWN_BODY =
            String.format("{'issuer' : '%s'}", DUMMY_ISSUER);
}
