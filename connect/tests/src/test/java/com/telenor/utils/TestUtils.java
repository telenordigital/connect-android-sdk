package com.telenor.utils;

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

public class TestUtils {

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

    public static class CustomRobolectricTestRunner extends RobolectricTestRunner {
        public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
            String pathToMerged = "build/intermediates/res/merged/" + BuildConfig.BUILD_TYPE;
            File f = new File(pathToMerged);
            if (f.exists()) {
                System.setProperty("android.resources", pathToMerged);
            }

        }
    }

    public static final WellKnownAPI WELL_KNOWN_API_THAT_FAILS =
        new WellKnownAPI() {
            @Override
            public void getWellKnownConfig(Callback<WellKnownConfig> callback) {
                callback.failure(null);
            }
        };

    public static final OperatorDiscoveryAPI A_VALID_OPERATOR_DISCOVERY_API =
            new OperatorDiscoveryAPI() {

                @Override
                public void getOperatorDiscoveryResult_ForMccMnc(
                        String auth,
                        String redirectUrl,
                        String identifiedMcc,
                        String identifiedMnc,
                        Callback<OperatorDiscoveryResult> callback) {
                    Gson gson = new Gson();
                    OperatorDiscoveryResult odr = gson.fromJson(
                            A_VALID_OPERATOR_DISCOVERY_BODY,
                            OperatorDiscoveryResult.class);
                    callback.success(odr, null);
                }
            };

    public static final OperatorDiscoveryAPI A_FAILING_OPERATOR_DISCOVERY_API =
            new OperatorDiscoveryAPI() {

                @Override
                public void getOperatorDiscoveryResult_ForMccMnc(
                        String auth,
                        String redirectUrl,
                        String identifiedMcc,
                        String identifiedMnc,
                        Callback<OperatorDiscoveryResult> callback) {
                    callback.failure(null);
                }
            };


    public static <T> Map<String, T> grabMap(String key, T api) {
        Map<String, T> map = new HashMap<>();
        map.put(key, api);
        return map;
    }

    private static String A_VALID_OPERATOR_DISCOVERY_BODY =
            new StringBuilder()
                .append("{")
                .append("  'ttl': 1498403492,")
                .append("  'response': {")
                .append("    'client_id': 'dummy-client',")
                .append("    'client_secret': 'dummy-secret',")
                .append("    'serving_operator': 'dummy-operator',")
                .append("    'country': 'Foreignia',")
                .append("    'currency': 'BTC',")
                .append("    'client_name': 'dummy-client-name',")
                .append("    'apis': {")
                .append("      'operatorid': {")
                .append("        'link': [")
                .append("          {")
                .append("            'href': 'https://dummy/operator/oauth/authorize',")
                .append("            'rel': 'authorization'")
                .append("          },")
                .append("          {")
                .append("            'href': 'https://https://dummy/operator/oauth/token',")
                .append("            'rel': 'token'")
                .append("          },")
                .append("          {")
                .append("            'href': 'https://https://dummy/operator/oauth/oauth/userinfo',")
                .append("            'rel': 'userinfo'")
                .append("          },")
                .append("          {")
                .append("            'href': 'https://https://dummy/operator/oauth/oauth/token',")
                .append("            'rel': 'tokenrefresh'")
                .append("         },")
                .append("         {")
                .append("           'href': 'https://https://dummy/operator/oauth/oauth/revoke',")
                .append("           'rel': 'tokenrevoke'")
                .append("         }")
                .append("        ]")
                .append("      }")
                .append("    }")
                .append("  }")
                .append("}")
                .toString();
}
