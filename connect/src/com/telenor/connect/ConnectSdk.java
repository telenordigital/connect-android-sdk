package com.telenor.connect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.id.AccessTokenCallback;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.IdToken;
import com.telenor.connect.id.TokenStore;
import com.telenor.connect.id.UserInfo;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.ui.ConnectWebFragment;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.RestHelper;
import com.telenor.connect.utils.Validator;
import com.telenor.mobileconnect.MobileConnectSdkProfile;
import com.telenor.mobileconnect.SimCardStateChangedBroadcastReceiver;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit.Callback;

public final class ConnectSdk {

    private static String sLastAuthState;
    private static ArrayList<Locale> sLocales;
    private static String sPaymentCancelUri;
    private static String sPaymentSuccessUri;
    private static SdkProfile sdkProfile;
    private static ConnectivityManager connectivityManager;
    private static volatile Network cellularNetwork;
    private static volatile Network wifiNetwork;

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CLIENT_ID_PROPERTY = "com.telenor.connect.CLIENT_ID";

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CONFIDENTIAL_CLIENT_PROPERTY = "com.telenor.connect.CONFIDENTIAL_CLIENT";

    /**
     * The key to for the payment cancel URI in the Android manifest.
     */
    public static final String PAYMENT_CANCEL_URI_PROPERTY = "com.telenor.connect.PAYMENT_CANCEL_URI";

    /**
     * The key to for the payment success URI in the Android manifest.
     */
    public static final String PAYMENT_SUCCESS_URI_PROPERTY = "com.telenor.connect.PAYMENT_SUCCESS_URI";

    /**
     * The key for the redirect URI in the Android manifest.
     */
    public static final String REDIRECT_URI_PROPERTY = "com.telenor.connect.REDIRECT_URI";

    /**
     * The key to enable the staging environment in the Android manifest.
     */
    public static final String USE_STAGING_PROPERTY = "com.telenor.connect.USE_STAGING";

    public static final int NO_CUSTOM_LAYOUT = -1;

    public static final String ACTION_LOGIN_STATE_CHANGED =
            "com.telenor.connect.ACTION_LOGIN_STATE_CHANGED";

    public static final String EXTRA_PAYMENT_LOCATION =
            "com.telenor.connect.EXTRA_PAYMENT_LOCATION";

    public static final String EXTRA_CONNECT_TOKENS =
            "com.telenor.connect.EXTRA_CONNECT_TOKENS";

    public static final int MAX_REDIRECTS_TO_FOLLOW_FOR_HE = 5;

    public static SdkProfile getSdkProfile() {
        Validator.sdkInitialized();
        return sdkProfile;
    }

    public static synchronized void authenticate(
            Activity activity,
            int requestCode,
            String... scopeTokens) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("scope", TextUtils.join(" ", scopeTokens));
        authenticate(activity, parameters, requestCode);
    }

    public static synchronized void authenticate(
            final Activity activity,
            final Map<String, String> parameters,
            final int requestCode) {
        authenticate(activity, parameters, NO_CUSTOM_LAYOUT, requestCode);
    }

    private static Intent getAuthIntent(Uri startUri) {
        final Intent intent = new Intent();
        intent.setClass(getContext(), ConnectActivity.class);
        intent.setAction(ConnectUtils.LOGIN_ACTION);
        final String url = startUri.toString();
        intent.putExtra(ConnectUtils.LOGIN_AUTH_URI, url);
        return intent;
    }

    public static synchronized void authenticate(final Activity activity,
                                                 final Map<String, String> parameters,
                                                 final int customLoadingLayout,
                                                 final int requestCode) {
        Validator.sdkInitialized();
        final ArrayList<String> uiLocales = getUiLocales();
        sdkProfile.onStartAuthorization(
                parameters,
                uiLocales,
                new SdkProfile.OnStartAuthorizationCallback() {

                    @Override
                    public void onSuccess(Uri authorizationStartUri) {
                        startAuthActivity(authorizationStartUri, false);
                    }

                    @Override
                    public void onDivert(Uri diversionUri) {
                        startAuthActivity(diversionUri, true);
                    }

                    @Override
                    public void onError() {
                        showAuthCancelMessage(activity);
                    }

                    private void startAuthActivity(
                            Uri startUri,
                            boolean isDivertedToFetchInitUri) {

                        sLastAuthState = parameters.get("state");

                        Intent intent = getAuthIntent(startUri);
                        intent.putExtra(
                                ConnectUtils.WELL_KNOWN_CONFIG_EXTRA,
                                sdkProfile.getWellKnownConfig());

                        if (isDivertedToFetchInitUri) {
                            intent.putExtra(
                                    ConnectUtils.IS_DIVERTED_TO_FETCH_INIT_URI,
                                    isDivertedToFetchInitUri);
                            intent.putExtra(
                                    ConnectUtils.PARAM_SNAPHOT_EXTRA,
                                    new HashMap<>(parameters));
                            intent.putExtra(
                                    ConnectUtils.UI_LOCALES_SNAPHOT_EXTRA,
                                    uiLocales);
                        }

                        if (customLoadingLayout != NO_CUSTOM_LAYOUT) {
                            intent.putExtra(
                                    ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA,
                                    customLoadingLayout);
                        }

                        activity.startActivityForResult(intent, requestCode);
                    }
        });
    }

    public static void showAuthCancelMessage(Activity activity) {
        Toast.makeText(
                activity,
                R.string.com_telenor_authorization_cancelled,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Get a {@code Fragment} that can be used for authorizing and getting a tokens.
     * {@code Activity} that uses the {@code Fragment} must implement {@code ConnectCallback}.
     *
     * @param parameters authorization parameters
     * @return authorization fragment
     */

    public static Fragment getAuthFragment(Map<String, String> parameters) {
        Validator.sdkInitialized();
        Validator.connectIdOnly();

        final Fragment fragment = new ConnectWebFragment();
        Intent authIntent = getAuthIntent(parameters);
        String action = authIntent.getAction();
        Bundle bundle = new Bundle(authIntent.getExtras());
        bundle.putString(ConnectUrlHelper.ACTION_ARGUMENT, action);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    /**
     * Get a valid Access Token. If a non-expired one is available, that will be given to the
     * callback {@code onSuccess(String accessToken)} method.
     * <p>
     * If it is expired, it will be refreshed and then returned. This requires a network call.
     *
     * @param callback callback that will be called on success or failure to update.
     * @throws ConnectRefreshTokenMissingException if no Request Token is available
     */
    public static synchronized void getValidAccessToken(AccessTokenCallback callback) {
        Validator.sdkInitialized();
        sdkProfile.getConnectIdService().getValidAccessToken(callback);
    }

    public static synchronized String getAccessToken() {
        Validator.sdkInitialized();
        if (sdkProfile.isInitialized()) {
            return sdkProfile.getConnectIdService().getAccessToken();
        }
        return null;
    }

    /**
     * Get the expiration time of the signed in user. If no user is signed in returns {@code null}.
     *
     * @return the expiration time of the Access Token in form of a {@code Date}.
     */
    public static synchronized Date getAccessTokenExpirationTime() {
        Validator.sdkInitialized();
        return sdkProfile.getConnectIdService().getAccessTokenExpirationTime();
    }

    public static synchronized void getAccessTokenFromCode(String code, ConnectCallback callback) {
        Validator.sdkInitialized();
        sdkProfile.getConnectIdService().getAccessTokenFromCode(code, callback);
    }

    private static Intent getAuthIntent(Map<String, String> parameters) {
        final Intent intent = new Intent();
        intent.setClass(getContext(), ConnectActivity.class);
        intent.setAction(ConnectUtils.LOGIN_ACTION);
        final String url = getAuthorizeUriAndSetLastAuthState(parameters).toString();
        intent.putExtra(ConnectUtils.LOGIN_AUTH_URI, url);
        return intent;
    }

    private static synchronized Uri getAuthorizeUriAndSetLastAuthState(
            Map<String, String> parameters) {
        if (ConnectSdk.getClientId() == null) {
            throw new ConnectException("Client ID not specified in application manifest.");
        }
        if (ConnectSdk.getRedirectUri() == null) {
            throw new ConnectException("Redirect URI not specified in application manifest.");
        }

        if (parameters.get("scope") == null || parameters.get("scope").isEmpty()) {
            throw new IllegalStateException("Cannot log in without scope tokens.");
        }

        if (parameters.get("state") == null || parameters.get("state").isEmpty()) {
            parameters.put("state", UUID.randomUUID().toString());
        }
        sLastAuthState = parameters.get("state");

        return sdkProfile.getAuthorizeUri(parameters, getUiLocales());
    }

    public static HttpUrl getConnectApiUrl() {
        Validator.sdkInitialized();
        return sdkProfile.getApiUrl();
    }

    public static Context getContext() {
        Validator.sdkInitialized();
        return sdkProfile.getContext();
    }

    public static String getClientId() {
        Validator.sdkInitialized();
        return sdkProfile.getClientId();
    }

    public static String getLastAuthenticationState() {
        Validator.sdkInitialized();
        return sLastAuthState;
    }

    public static List<Locale> getLocales() {
        Validator.sdkInitialized();
        return sLocales;
    }

    public static String getPaymentCancelUri() {
        Validator.sdkInitialized();
        return sPaymentCancelUri;
    }

    public static String getPaymentSuccessUri() {
        Validator.sdkInitialized();
        return sPaymentSuccessUri;
    }

    public static String getRedirectUri() {
        Validator.sdkInitialized();
        return sdkProfile.getRedirectUri();
    }

    private static ArrayList<String> getUiLocales() {
        ArrayList<String> locales = new ArrayList<>();
        if (ConnectSdk.getLocales() != null && !ConnectSdk.getLocales().isEmpty()) {
            for (Locale locale : ConnectSdk.getLocales()) {
                locales.add(locale.toString());
                locales.add(locale.getLanguage());
            }
        }
        locales.add(Locale.getDefault().toString());
        locales.add(Locale.getDefault().getLanguage());
        return locales;
    }

    public static void initializePayment(Context context, String transactionLocation) {
        Validator.sdkInitialized();
        if (ConnectSdk.getPaymentSuccessUri() == null
                || ConnectSdk.getPaymentCancelUri() == null) {
            throw new ConnectException("Payment success or cancel URI not specified in application"
                    + "manifest.");
        }

        Intent intent = new Intent();
        intent.setClass(getContext(), ConnectActivity.class);
        intent.putExtra(ConnectSdk.EXTRA_PAYMENT_LOCATION, transactionLocation);
        intent.setAction(ConnectUtils.PAYMENT_ACTION);

        Activity activity = (Activity) context;
        activity.startActivityForResult(intent, 1);
    }

    public static String getExpectedIssuer() {
        Validator.sdkInitialized();
        return sdkProfile.getExpectedIssuer();
    }

    public static List<String> getExpectedAudiences() {
        Validator.sdkInitialized();
        return sdkProfile.getExpectedAudiences();
    }

    public static synchronized boolean isConfidentialClient() {
        Validator.sdkInitialized();
        return sdkProfile.isConfidentialClient();
    }

    public static synchronized boolean isInitialized() {
        return sdkProfile != null;
    }

    public static void logout() {
        Validator.sdkInitialized();
        sdkProfile.getConnectIdService().revokeTokens(getContext());
    }

    public static synchronized void sdkInitialize(Context context) {
        if (isInitialized()) {
            return;
        }


        Validator.notNull(context, "context");
        ConnectSdkProfile profile = loadConnectConfig(context);
        sdkProfile = profile;
        profile.setConnectIdService(
                new ConnectIdService(
                        new TokenStore(context),
                        RestHelper.getConnectApi(getConnectApiUrl().toString()),
                        profile.getClientId(),
                        profile.getRedirectUri()));

        initializeCommonComponents();
    }

    public static String getMccMnc() {
        TelephonyManager tel
                = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tel.getNetworkOperator();
    }

    public static void setLocales(Locale... locales) {
        sLocales = new ArrayList<Locale>(Arrays.asList(locales));
    }

    public static void setLocales(ArrayList<Locale> locales) {
        sLocales = locales;
    }

    /**
     * Manually update the access token.
     *
     * @param callback callback that will be called on success or failure to update.
     */
    public static void updateTokens(AccessTokenCallback callback) {
        Validator.sdkInitialized();
        sdkProfile.getConnectIdService().updateTokens(callback);
    }

    private static ConnectSdkProfile loadConnectConfig(Context context) {

        ApplicationInfo ai = getApplicationInfo(context);
        if (ai == null || ai.metaData == null) {
            throw new ConnectException("No application metadata was found.");
        }

        ConnectSdkProfile profile = new ConnectSdkProfile(
                context,
                fetchBooleanProperty(ai, USE_STAGING_PROPERTY),
                fetchBooleanProperty(ai, CONFIDENTIAL_CLIENT_PROPERTY));

        Object clientIdObject = ai.metaData.get(CLIENT_ID_PROPERTY);
        if (clientIdObject instanceof String) {
            String clientIdString = (String) clientIdObject;
            profile.setClientId(clientIdString);
        }

        Object redirectUriObject = ai.metaData.get(REDIRECT_URI_PROPERTY);
        if (redirectUriObject instanceof String) {
            profile.setRedirectUri((String) redirectUriObject);
        }

        Object paymentCancelUriObject = ai.metaData.get(PAYMENT_CANCEL_URI_PROPERTY);
        if (paymentCancelUriObject instanceof String) {
            String paymentCancelUriString = (String) paymentCancelUriObject;
            sPaymentCancelUri = paymentCancelUriString;
        }

        Object paymentSuccessUriObject = ai.metaData.get(PAYMENT_SUCCESS_URI_PROPERTY);
        if (paymentSuccessUriObject instanceof String) {
            String paymentSuccessUriString = (String) paymentSuccessUriObject;
            sPaymentSuccessUri = paymentSuccessUriString;
        }
        return profile;
    }

    private static ApplicationInfo getApplicationInfo(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    private static boolean fetchBooleanProperty(ApplicationInfo appInfo, String propertyName) {
        if (appInfo == null) {
            return false;
        }
        if (appInfo.metaData == null) {
            return false;
        }
        Object booleanPropertyObject = appInfo.metaData.get(propertyName);
        if (!(booleanPropertyObject instanceof Boolean)) {
            return false;
        }
        return (Boolean) booleanPropertyObject;
    }

    public static WellKnownAPI.WellKnownConfig getWellKnownConfig() {
        Validator.sdkInitialized();
        return sdkProfile.getWellKnownConfig();
    }

    /**
     * @return the subject's ID (sub), if one is signed in. Otherwise {@code null}.
     * @deprecated use {@code getIdToken()} instead to access user information.
     */
    @Deprecated
    public static String getSubjectId() {
        Validator.sdkInitialized();
        IdToken idToken = sdkProfile.getConnectIdService().getIdToken();
        return idToken != null ? idToken.getSubject() : null;
    }

    /**
     * Returns the {@code IdToken} of the signed in user, otherwise {@code null}. The presence of
     * the fields depend on the scope and claim variables that were given at sign in time.
     * See http://docs.telenordigital.com/apis/connect/id/authentication.html for more details.
     *
     * @return the ID token for the user
     */
    public static IdToken getIdToken() {
        Validator.sdkInitialized();
        return sdkProfile.getConnectIdService().getIdToken();
    }

    /**
     * Fetches the logged in user's info from the /oauth/userinfo endpoint.
     * See http://docs.telenordigital.com/apis/connect/id/authentication.html#authorization-server-user-information
     * for more details on the scope and claims preconditions needed in order to get the info
     * needed.
     *
     * @param userInfoCallback the callback that will be called on after the response is received
     * @throws ConnectNotInitializedException if no user is signed in.
     */
    public static void getUserInfo(Callback<UserInfo> userInfoCallback) {
        Validator.sdkInitialized();
        sdkProfile.getConnectIdService().getUserInfo(userInfoCallback);
    }

    public static synchronized void sdkInitializeMobileConnect(
            Context context,
            OperatorDiscoveryConfig operatorDiscoveryConfig) {
        if (isInitialized()) {
            return;
        }
        sdkProfile = new MobileConnectSdkProfile(
                context,
                operatorDiscoveryConfig,
                fetchBooleanProperty(getApplicationInfo(context), CONFIDENTIAL_CLIENT_PROPERTY));
        context.registerReceiver(
                SimCardStateChangedBroadcastReceiver.getReceiver(),
                SimCardStateChangedBroadcastReceiver.getIntentFilter());

        initializeCommonComponents();
    }

    public static synchronized void sdkReinitializeMobileConnect() {
        if (!isInitialized()) {
            return;
        }
        MobileConnectSdkProfile profile = (MobileConnectSdkProfile) sdkProfile;
        profile.deInitialize();
    }

    public static ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    public static Network getCellularNetwork() {
        return cellularNetwork;
    }

    public static Network getWifiNetwork() {
        return wifiNetwork;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void initalizeCellularNetwork() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        connectivityManager.requestNetwork(
                networkRequest,
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        cellularNetwork = network;
                    }
                }
        );
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void initalizeWiFiNetwork() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        connectivityManager.requestNetwork(
                networkRequest,
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        wifiNetwork = network;
                    }
                }
        );
    }

    /**
     * Initialize components common to both Mobile Connect and ConnectID SDK profiles
     */
    public static synchronized void initializeCommonComponents() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager) {
            return;
        }
        initalizeCellularNetwork();
        initalizeWiFiNetwork();
    }
}
