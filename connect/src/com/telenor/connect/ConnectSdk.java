package com.telenor.connect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsSession;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.telenor.connect.headerenrichment.DismissDialogCallback;
import com.telenor.connect.headerenrichment.HeLogic;
import com.telenor.connect.headerenrichment.ShowLoadingCallback;
import com.telenor.connect.headerenrichment.HeTokenResponse;
import com.telenor.connect.headerenrichment.HeTokenCallback;
import com.telenor.connect.id.AccessTokenCallback;
import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectStore;
import com.telenor.connect.id.IdToken;
import com.telenor.connect.id.UserInfo;
import com.telenor.connect.id.IdProvider;
import com.telenor.connect.sms.SmsBroadcastReceiver;
import com.telenor.connect.sms.SmsHandler;
import com.telenor.connect.sms.SmsPinParseUtil;
import com.telenor.connect.sms.SmsRetrieverUtil;
import com.telenor.connect.ui.ConnectActivity;
import com.telenor.connect.ui.ConnectWebFragment;
import com.telenor.connect.ui.ConnectWebViewLoginButton;
import com.telenor.connect.utils.ConnectUrlHelper;
import com.telenor.connect.utils.ConnectUtils;
import com.telenor.connect.utils.TurnOnMobileDataDialogAnalytics;
import com.telenor.connect.utils.RestHelper;
import com.telenor.connect.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ConnectSdk {
    private static final int SESSION_TIMEOUT_MINUTES = 10;
    private static ArrayList<Locale> sLocales;
    private static ConnectStore connectStore;
    private static WellKnownConfigStore lastSeenWellKnownConfigStore;
    private static ConnectIdService connectIdService;
    private static Context context;
    private static boolean confidentialClient;
    private static volatile WellKnownAPI.WellKnownConfig wellKnownConfig;
    private static volatile boolean isInitialized = false;
    private static String clientId;
    private static String redirectUri;
    private static boolean enableTurnOnMobileDataDialog;
    private static IdProvider idProvider;
    private static boolean useStaging;
    private static SmsBroadcastReceiver smsBroadcastReceiver;
    private static volatile String advertisingId;
    private static volatile long tsSdkInitialization;
    private static volatile long tsLoginButtonClicked;
    private static volatile long tsRedirectUrlInvoked;
    private static volatile long tsTokenResponseReceived;
    private static volatile String logSessionId;
    private static volatile Date logSessionIdSetTime;

    private static TurnOnMobileDataDialogAnalytics turnOnMobileDataDialogAnalytics;

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CLIENT_ID_PROPERTY = "com.telenor.connect.CLIENT_ID";

    /**
     * The key for the client ID in the Android manifest.
     */
    public static final String CONFIDENTIAL_CLIENT_PROPERTY = "com.telenor.connect.CONFIDENTIAL_CLIENT";

    /**
     * The key for the redirect URI in the Android manifest.
     */
    public static final String REDIRECT_URI_PROPERTY = "com.telenor.connect.REDIRECT_URI";

    public static final String ENABLE_TURN_ON_MOBILE_DATA_DIALOG_PROPERTY
            = "com.telenor.connect.ENABLE_TURN_ON_MOBILE_DATA_DIALOG";

    public static final String ACTION_LOGIN_STATE_CHANGED =
            "com.telenor.connect.ACTION_LOGIN_STATE_CHANGED";

    public static final String EXTRA_CONNECT_TOKENS =
            "com.telenor.connect.EXTRA_CONNECT_TOKENS";

    public static synchronized void authenticate(
            final CustomTabsSession session,
            final Map<String, String> parameters,
            final BrowserType browserType,
            final Activity activity,
            final ShowLoadingCallback showLoadingCallback,
            final DismissDialogCallback dismissDialogCallback) {
        handleButtonClickedAnalytics();
        if (dismissDialogCallback != null) {
            handleMobileDataAnalytics(dismissDialogCallback.getAnalytics());
        }
        HeTokenCallback heTokenCallback = new HeTokenCallback() {
            @Override
            public void done() {
                Uri authorizeUri = getAuthorizeUri(parameters, browserType);
                launchChromeCustomTabAuthentication(session, authorizeUri, activity);
            }
        };
        HeLogic.handleHeToken(parameters, showLoadingCallback, heTokenCallback, logSessionId, idProvider, useStaging, dismissDialogCallback);
    }

    private static void handleButtonClickedAnalytics() {
        updateLogSessionIdIfTooOld();
        tsLoginButtonClicked = System.currentTimeMillis();
    }

    private static void handleMobileDataAnalytics(TurnOnMobileDataDialogAnalytics analytics) {
        turnOnMobileDataDialogAnalytics = analytics;
    }

    private static void updateLogSessionIdIfTooOld() {
        Date _10MinutesAgo = getSessionTimeoutDate();
        if (logSessionIdSetTime.before(_10MinutesAgo)) {
            setRandomLogSessionId();
        }
    }

    private static Date getSessionTimeoutDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -SESSION_TIMEOUT_MINUTES);
        return calendar.getTime();
    }

    private static Uri getAuthorizeUri(Map<String, String> parameters, BrowserType browserType) {
        boolean failedToGetToken = HeLogic.failedToGetToken();
        HeTokenResponse heTokenResponse = HeLogic.getHeTokenResponse();
        return ConnectUrlHelper.getAuthorizeUri(parameters, browserType, failedToGetToken ? null : heTokenResponse.getToken());
    }

    private static void launchChromeCustomTabAuthentication(
            final CustomTabsSession session,
            Uri authorizeUri,
            final Activity activity) {
        SmsRetrieverUtil.startSmsRetriever(getContext());
        smsBroadcastReceiver = new SmsBroadcastReceiver(new SmsHandler() {
            @Override
            public void receivedSms(String messageBody) {
                String pin = SmsPinParseUtil.findPin(messageBody);
                if (pin == null) {
                    return;
                }
                safeUnregisterAndRemoveBroadcastReceiver();
                String url = ConnectUrlHelper.getSubmitPinUrl(pin);
                Uri uri = Uri.parse(url);
                launchUrlInCustomTab(activity, session, uri);
            }
        });
        getContext().registerReceiver(smsBroadcastReceiver, SmsRetrieverUtil.SMS_FILTER);
        launchUrlInCustomTab(activity, session, authorizeUri);
    }

    private static void launchUrlInCustomTab(Activity activity, CustomTabsSession session, Uri uri) {
        if (activity == null) {
            Log.e(ConnectUtils.LOG_TAG, "Failed to launch url in custom tab: activity is null.");
            return;
        }
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);
        CustomTabsIntent cctIntent = builder.build();
        Intent intent = cctIntent.intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + activity.getPackageName()));
        }
        cctIntent.launchUrl(activity, uri);
    }

    public static synchronized void authenticate(
            Activity activity,
            int requestCode,
            String... scopeTokens) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", TextUtils.join(" ", scopeTokens));
        authenticate(activity, parameters, requestCode);
    }

    public static synchronized void authenticate(
            final Activity activity,
            final Map<String, String> parameters,
            final int requestCode) {
        Validator.sdkInitialized();
        authenticate(activity, parameters, ConnectWebViewLoginButton.NO_CUSTOM_LAYOUT, requestCode, null, null);
    }

    public static synchronized void authenticate(final Activity activity,
                                                 final Map<String, String> parameters,
                                                 final int customLoadingLayout,
                                                 final int requestCode,
                                                 final ShowLoadingCallback showLoadingCallback,
                                                 final DismissDialogCallback dismissDialogCallback) {
        Validator.sdkInitialized();
        handleButtonClickedAnalytics();
        if (dismissDialogCallback != null) {
            handleMobileDataAnalytics(dismissDialogCallback.getAnalytics());
        }
        HeTokenCallback heTokenCallback = new HeTokenCallback() {
            @Override
            public void done() {
                Intent intent = getAuthIntent(parameters);
                if (customLoadingLayout != ConnectWebViewLoginButton.NO_CUSTOM_LAYOUT) {
                    intent.putExtra(ConnectUtils.CUSTOM_LOADING_SCREEN_EXTRA, customLoadingLayout);
                }
                activity.startActivityForResult(intent, requestCode);
            }
        };
        HeLogic.handleHeToken(parameters, showLoadingCallback, heTokenCallback, logSessionId, idProvider, useStaging, dismissDialogCallback);
    }

    private static Intent getAuthIntent(Map<String, String> parameters) {
        Intent intent = new Intent();
        intent.setClass(getContext(), ConnectActivity.class);
        intent.setAction(ConnectUtils.LOGIN_ACTION);
        String mccMnc = getMccMnc();
        if (!TextUtils.isEmpty(mccMnc) && wellKnownConfig != null &&
                !(wellKnownConfig.getNetworkAuthenticationTargetIps().isEmpty()
                        && wellKnownConfig.getNetworkAuthenticationTargetUrls().isEmpty())) {
            parameters.put("login_hint", String.format("MCCMNC:%s", mccMnc));
        }
        String url = getAuthorizeUri(parameters, BrowserType.WEB_VIEW).toString();
        intent.putExtra(ConnectUtils.LOGIN_AUTH_URI, url);
        return intent;
    }

    /**
     * @deprecated Undocumented feature that might be removed in the future.
     *
     * Get a {@code Fragment} that can be used for authorizing and getting a tokens.
     * {@code Activity} that uses the {@code Fragment} must implement {@code ConnectCallback}.
     *
     * @param parameters authorization parameters
     * @return authorization fragment
     */
    @Deprecated
    public static Fragment getAuthFragment(Map<String, String> parameters) {
        Validator.sdkInitialized();

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
     * callback {@code success(String accessToken)} method immediately.
     * <p>
     * If it is expired, it will be asynchronously refreshed and then returned in the same way.
     *
     * @param callback callback that will be called on success or failure to update.
     */
    public static synchronized void getValidAccessToken(AccessTokenCallback callback) {
        Validator.sdkInitialized();
        connectIdService.getValidAccessToken(callback);
    }

    public static synchronized String getAccessToken() {
        Validator.sdkInitialized();
        return connectIdService.getAccessToken();
    }

    /**
     * Get the expiration time of the signed in user. If no user is signed in returns {@code null}.
     *
     * @return the expiration time of the Access Token in form of a {@code Date}.
     */
    public static synchronized Date getAccessTokenExpirationTime() {
        Validator.sdkInitialized();
        return connectIdService.getAccessTokenExpirationTime();
    }

    public static synchronized void getAccessTokenFromCode(String code, final ConnectCallback callback) {
        Validator.sdkInitialized();
        tsRedirectUrlInvoked = System.currentTimeMillis();
        connectIdService.getAccessTokenFromCode(code, new ConnectCallback() {
            @Override
            public void onSuccess(Object successData) {
                tsTokenResponseReceived = System.currentTimeMillis();
                if (callback != null) {
                    callback.onSuccess(successData);
                }
                sendAnalyticsData();
            }

            @Override
            public void onError(Object errorData) {
                tsTokenResponseReceived = System.currentTimeMillis();
                if (callback != null) {
                    callback.onError(errorData);
                }
                sendAnalyticsData();
            }
        });
    }

    private static void sendAnalyticsData() {
        sendAnalyticsData(null);
    }

    public static void sendAnalyticsData(Throwable throwable) {
        String analyticsEndpoint;
        // This is bad practice. The source of the issue must be found.
        try {
            analyticsEndpoint = getWellKnownConfig().getAnalyticsEndpoint();
        } catch (NullPointerException e) {
            Log.e(ConnectUtils.LOG_TAG, "Failed to send analytics data, wellKnownConfig is null.");
            return;
        }
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager != null ? manager.getNetworkOperatorName() : null;
        boolean accessTokenIsEmpty = getAccessToken() == null || getAccessToken().isEmpty();
        HashMap<String, Object> debugInformation = new HashMap<>();
        debugInformation.put("activeNetworkInfo", HeLogic.getActiveNetworkInfo());
        debugInformation.put("cellularNetworkInfo", HeLogic.getCellularNetworkInfo());
        debugInformation.put("deviceTimestamp", new Date());
        debugInformation.put("carrierName", carrierName);
        debugInformation.put("sdkVersion", BuildConfig.VERSION_NAME);
        debugInformation.put("isAccessTokenEmpty", accessTokenIsEmpty);
        if (throwable != null) {
            debugInformation.put("exception", throwable.getMessage());
            debugInformation.put("exceptionStackTrace", throwable.getStackTrace());
        }

        if (turnOnMobileDataDialogAnalytics == null) {
            turnOnMobileDataDialogAnalytics = new TurnOnMobileDataDialogAnalytics(
                    enableTurnOnMobileDataDialog,
                    false,
                    false,
                    false
            );
        }
        String accessToken = getAccessToken();
        final String auth = accessToken != null ? "Bearer " + accessToken : null;
        final String subject = getIdToken() != null ? getIdToken().getSubject() : null;
        RestHelper.getAnalyticsApi(analyticsEndpoint).sendAnalyticsData(
                auth,
                new AnalyticsAPI.SDKAnalyticsData(
                        getApplicationName(),
                        getApplicationVersion(),
                        subject,
                        getLogSessionId(),
                        getAdvertisingId(),
                        tsSdkInitialization,
                        tsLoginButtonClicked,
                        tsRedirectUrlInvoked,
                        tsTokenResponseReceived,
                        debugInformation,
                        turnOnMobileDataDialogAnalytics
                ))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Log.e(ConnectUtils.LOG_TAG, "Failed to send analytics data");
                        }
                        setRandomLogSessionId();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable error) {
                        Log.e(ConnectUtils.LOG_TAG, "Failed to send analytics data", error);
                        setRandomLogSessionId();
                    }
                });
    }

    public static void setRandomLogSessionId() {
        logSessionId = UUID.randomUUID().toString();
        logSessionIdSetTime = new Date();
    }

    public static Context getContext() {
        Validator.sdkInitialized();
        return context;
    }

    public static String getClientId() {
        Validator.sdkInitialized();
        return clientId;
    }

    public static boolean isTurnOnMobileDataDialogEnabled() {
        Validator.sdkInitialized();
        return enableTurnOnMobileDataDialog;
    }

    public static ArrayList<Locale> getLocales() {
        Validator.sdkInitialized();
        return sLocales;
    }

    public static String getRedirectUri() {
        Validator.sdkInitialized();
        return redirectUri;
    }

    public static ArrayList<String> getUiLocales() {
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

    public static String getExpectedIssuer() {
        Validator.sdkInitialized();
        if (getWellKnownConfig() != null) {
            return getWellKnownConfig().getIssuer();
        }
        return ConnectUrlHelper.getConnectApiUrl(idProvider, useStaging) + ConnectUrlHelper.OAUTH_PATH;
    }

    public static List<String> getExpectedAudiences() {
        Validator.sdkInitialized();
        return Collections.singletonList(clientId);
    }

    public static synchronized boolean isConfidentialClient() {
        Validator.sdkInitialized();
        return confidentialClient;
    }

    public static synchronized boolean isInitialized() {
        return isInitialized;
    }

    public static void logout() {
        Validator.sdkInitialized();
        connectIdService.logOut(getContext());
    }

    /**
     * @deprecated This method is deprecated and would be removed soon.
     * Please, use {@link #sdkInitialize(Context, IdProvider, boolean) sdkInitialize} instead
     */
    @Deprecated
    public static synchronized void sdkInitialize(Context applicationContext, boolean useStagingEnvironment) {
        sdkInitialize(applicationContext, IdProvider.CONNECT_ID, useStagingEnvironment);
    }

    public static synchronized void sdkInitialize(Context applicationContext) {
        sdkInitialize(applicationContext, IdProvider.CONNECT_ID, true);
    }

    public static synchronized void sdkInitialize(Context applicationContext,
                                                  IdProvider provider,
                                                  boolean useStagingEnvironment) {
        if (isInitialized()) {
            return;
        }
        context = applicationContext;
        Validator.notNull(context, "context");

        useStaging = useStagingEnvironment;
        idProvider = provider;
        loadConnectConfig(context);
        connectStore = new ConnectStore(context);
        lastSeenWellKnownConfigStore = new WellKnownConfigStore(context);
        wellKnownConfig = lastSeenWellKnownConfigStore.get();
        String apiUrl = ConnectUrlHelper.getConnectApiUrl(provider, useStaging).toString();
        connectIdService = new ConnectIdService(
                connectStore,
                RestHelper.getConnectApi(apiUrl),
                clientId,
                redirectUri);
        setRandomLogSessionId();
        boolean noSignedInUser = connectIdService.getAccessToken() == null;
        boolean noStoredWellKnownConfig = wellKnownConfig == null;
        if (noSignedInUser || noStoredWellKnownConfig) {
            updateWellKnownConfig(apiUrl);
        }

        HeLogic.initializeNetworks(context, idProvider, useStaging);
        initializeAdvertisingId(context);
        isInitialized = true;
        tsSdkInitialization = System.currentTimeMillis();
    }

    private static void updateWellKnownConfig(String apiUrl) {
        RestHelper.
                getWellKnownApi(apiUrl).getWellKnownConfig()
                .enqueue(new Callback<WellKnownAPI.WellKnownConfig>() {
                    @Override
                    public void onResponse(Call<WellKnownAPI.WellKnownConfig> call,
                                           Response<WellKnownAPI.WellKnownConfig> response) {
                        if (response.isSuccessful()) {
                            final WellKnownAPI.WellKnownConfig config = response.body();
                            wellKnownConfig = config;
                            lastSeenWellKnownConfigStore.set(config);
                        } else {
                            wellKnownConfig = null;
                        }
                    }

                    @Override
                    public void onFailure(Call<WellKnownAPI.WellKnownConfig> call, Throwable error) {
                        wellKnownConfig = null;
                    }
                });
    }

    private static String getMccMnc() {
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
        connectIdService.updateTokens(callback);
    }

    private static void loadConnectConfig(Context context) {
        ApplicationInfo ai = getApplicationInfo(context);
        if (ai == null || ai.metaData == null) {
            throw new ConnectException("No application metadata was found.");
        }
        confidentialClient = fetchBooleanProperty(ai, CONFIDENTIAL_CLIENT_PROPERTY);

        Object clientIdObject = ai.metaData.get(CLIENT_ID_PROPERTY);
        if (clientIdObject instanceof String) {
            clientId = (String) clientIdObject;
        }

        Object redirectUriObject = ai.metaData.get(REDIRECT_URI_PROPERTY);
        if (redirectUriObject instanceof String) {
            redirectUri = (String) redirectUriObject;
        }

        Object enableTurnOnMobileDataDialogObject = ai.metaData.get(ENABLE_TURN_ON_MOBILE_DATA_DIALOG_PROPERTY);
        if (enableTurnOnMobileDataDialogObject instanceof Boolean) {
            enableTurnOnMobileDataDialog = (boolean) enableTurnOnMobileDataDialogObject;
        }
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
        return wellKnownConfig;
    }

    /**
     * @return the subject's ID (sub), if one is signed in. Otherwise {@code null}.
     * @deprecated use {@code getIdToken()} instead to access user information.
     */
    @Deprecated
    public static String getSubjectId() {
        Validator.sdkInitialized();
        IdToken idToken = connectIdService.getIdToken();
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
        return connectIdService.getIdToken();
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
        connectIdService.getUserInfo(userInfoCallback);
    }

    /**
     * Checks if the Uri data of an {@code Intent} is the same as the apps registered Redirect Uri,
     * with state matching the saved state and a code which can be used to get a valid Access Token.
     * Useful for checking if an {@code Activity} was started by the system calling
     * {@code "example-clientid://oauth2callback?state=xyz&code=abc"}
     *
     * @param intent intent to check data element of.
     * @return true if getData() on the intent matches Redirect Uri, has valid state and code
     * query parameters.
     */
    public static boolean hasValidRedirectUrlCall(Intent intent) {
        final Uri data = intent.getData();
        if (data == null) {
            return false;
        }

        final boolean startsWithCorrect = data.toString().startsWith(getRedirectUri());
        if (!startsWithCorrect) {
            return false;
        }

        final String state = data.getQueryParameter("state");
        String originalState = connectStore.getSessionStateParam();
        if (originalState != null && !originalState.equals(state)) {
            return false;
        }

        final String code = data.getQueryParameter("code");
        if (code == null || code.isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean hasErrorRedirectUrlCall(Intent intent) {
        if (intent == null) {
            return false;
        }
        final Uri uri = intent.getData();
        if (uri == null) {
            return false;
        }

        final boolean startsWithCorrect = uri.toString().startsWith(getRedirectUri());
        if (!startsWithCorrect) {
            return false;
        }

        return uri.getQueryParameter("error") != null;
    }

    /**
     * Open self service url in custom tab or external browser. SDK has to be initialized to
     * perform this action.
     *
     * We don't do any warmup or optimizations on this component.
     *
     * @param context of activity from where method is called
     */
    public static void openSelfServicePage(Context context) {
        if (!isInitialized() || idProvider == IdProvider.CONNECT_ID) {
            return;
        }
        Uri selfServiceUri = Uri.parse(
                ConnectUrlHelper
                        .getSelfServiceUrl(idProvider, useStaging)
                        .newBuilder()
                        .addPathSegment("overview")
                        .build()
                        .uri()
                        .toString()
        );
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
        Intent intent = customTabsIntent.intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
        }
        customTabsIntent.launchUrl(context, selfServiceUri);
    }

    /**
     * If the intent has a valid Redirect Uri data element the query parameter authorization code
     * will be passed to the {@code getAccessTokenFromCode} method, with the given
     * ConnectCallback callback.
     *
     * @param intent intent to check data element of
     * @param callback callback that will be called upon by the getAccessTokenFromCode method
     *
     * @see #hasValidRedirectUrlCall
     * @see #getAccessTokenFromCode
     */
    public static void handleRedirectUriCallIfPresent(
            Intent intent, ConnectCallback callback) {
        if (!hasValidRedirectUrlCall(intent)) {
            return;
        }
        safeUnregisterAndRemoveBroadcastReceiver();
        final String code = getCodeFromIntent(intent);
        getAccessTokenFromCode(code, callback);
    }

    private static void safeUnregisterAndRemoveBroadcastReceiver() {
        try {
            getContext().unregisterReceiver(smsBroadcastReceiver);
        } catch (IllegalArgumentException ignored) {
        } finally {
            smsBroadcastReceiver = null;
        }
    }

    /**
     * Helper method to get the <b>code</b> parameter from an Intent's data Uri.
     * Example: An Intent with a data Uri of
     * {@code "example-clientid://oauth2callback?code=123&state=xyz"} will return "123".
     *
     * @param intent the Intent to get the code from.
     * @return code parameter of an Intent
     */
    public static String getCodeFromIntent(@NonNull Intent intent) {
        final Uri data = intent.getData();
        return data.getQueryParameter("code");
    }

    private static void initializeAdvertisingId(final Context context) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            new Thread(new Runnable() {
                public void run() {
                    AdvertisingIdClient.Info adInfo;
                    try {
                        adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                        advertisingId = adInfo.getId();
                    } catch (Exception e) {
                        Log.w(ConnectUtils.LOG_TAG, "Failed to read advertising id", e);
                    }
                }
            }).start();
        }
    }

    /**
     * @deprecated Use {@link HeLogic#getCellularNetwork()}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Deprecated
    public static Network getCellularNetwork() {
        return HeLogic.getCellularNetwork();
    }

    /**
     * @deprecated Use {@link HeLogic#getDefaultNetwork()} ()}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Network getDefaultNetwork() {
        return HeLogic.getDefaultNetwork();
    }

    private static String getAdvertisingId() {
        return advertisingId;
    }

    public static String getLogSessionId() {
        return logSessionId;
    }

    private static String getApplicationName() {
        try {
            return getContext().getApplicationInfo().loadLabel(getContext().getPackageManager()).toString();
        } catch (Exception e) {
            Log.e(ConnectUtils.LOG_TAG, "Failed to read application name", e);
            return "";
        }
    }

    private static String getApplicationVersion() {
        try {
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.e(ConnectUtils.LOG_TAG, "Failed to read application version", e);
            return "";
        }
    }

    public static IdProvider getIdProvider() {
        Validator.sdkInitialized();
        return idProvider;
    }

    public static void setIdProvider(IdProvider newIdProvider) {
        Validator.sdkInitialized();
        idProvider = newIdProvider;
    }

    public static boolean useStaging() {
        Validator.sdkInitialized();
        return useStaging;
    }

    public static ConnectStore getConnectStore() {
        Validator.sdkInitialized();
        return connectStore;
    }
}
