package com.telenor.mobileconnect;

import com.google.gson.Gson;
import com.telenor.TestHelper;
import com.telenor.connect.WellKnownAPI;
import com.telenor.connect.tests.BuildConfig;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;

public class MobileConnectTestHelper {

    public static final String MOCKED_OD_ENDPONT = "https://fake/operator/discovery";
    public static final Map<String, OperatorDiscoveryAPI> OPERATOR_DISCOVERY_API_MAP = new HashMap<>();


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

    private static String A_VALID_OPERATOR_DISCOVERY_BODY =
            new StringBuilder()
                    .append("  {")
                    .append("    'ttl': 1498403492,")
                    .append("    'response': {")
                    .append("      'client_id': 'dummy-client',")
                    .append("      'client_secret': 'dummy-secret',")
                    .append("      'serving_operator': 'dummy-operator',")
                    .append("      'country': 'Foreignia',")
                    .append("      'currency': 'BTC',")
                    .append("      'client_name': 'dummy-client-name',")
                    .append("      'apis': {")
                    .append("        'operatorid': {")
                    .append("          'link': [")
                    .append("            {")
                    .append("              'href': 'https://dummy/operator/oauth/authorize',")
                    .append("              'rel': 'authorization'")
                    .append("            },")
                    .append("            {")
                    .append("              'href': 'https://dummy/operator/oauth/token',")
                    .append("              'rel': 'token'")
                    .append("            },")
                    .append("            {")
                    .append("              'href': 'https://dummy/operator/oauth/oauth/userinfo',")
                    .append("              'rel': 'userinfo'")
                    .append("            },")
                    .append("            {")
                    .append("              'href': 'https://dummy/operator/oauth/oauth/token',")
                    .append("              'rel': 'tokenrefresh'")
                    .append("            },")
                    .append("            {")
                    .append(String.format("'href': '%s',", TestHelper.MOCKED_WELL_KNOWN_ENDPONT))
                    .append("              'rel': 'openid-configuration'")
                    .append("            },")
                    .append("            {")
                    .append("              'href': 'https://dummy/operator/oauth/oauth/revoke',")
                    .append("              'rel': 'tokenrevoke'")
                    .append("            }")
                    .append("          ]")
                    .append("        }")
                    .append("      }")
                    .append("    }")
                    .append("}")
                    .toString();

}
