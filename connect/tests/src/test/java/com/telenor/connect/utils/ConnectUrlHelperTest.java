package com.telenor.connect.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.ConnectSdkProfile;
import com.telenor.connect.ConnectTestHelper;
import com.telenor.connect.ParametersHolder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class ConnectUrlHelperTest {

    @Test
    public void paymentActionArgumentReturnsUrlArgument() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.PAYMENT_ACTION");

        arguments.putString(
                "com.telenor.connect.URL_ARGUMENT",
                "some-url-argument://");

        String pageUrl = ConnectUrlHelper.getPageUrl(arguments);
        assertThat(pageUrl, is("some-url-argument://"));
    }

    @Test(expected = IllegalStateException.class)
    public void loginActionArgumentThrowsOnNullActivity() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        ConnectUrlHelper.getPageUrl(arguments);
    }

    @Test(expected = IllegalStateException.class)
    public void loginActionArgumentThrowsOnNullActivityIntent() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        ConnectUrlHelper.getPageUrl(arguments);
    }

    @Test(expected = IllegalStateException.class)
    public void loginActionArgumentThrowsOnNullActivityIntentLoginAuthUri() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        Activity activity = new Activity();
        Intent intent = new Intent();
        intent.putExtras(arguments);
        activity.setIntent(intent);

        ConnectUrlHelper.getPageUrl(arguments);
    }

    @Test(expected = IllegalStateException.class)
    public void loginActionArgumentThrowsOnEmptyActivityIntentLoginAuthUri() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        arguments.putString("com.telenor.connect.LOGIN_AUTH_URI", "");

        Activity activity = new Activity();
        Intent intent = new Intent();
        intent.putExtras(arguments);
        activity.setIntent(intent);

        ConnectUrlHelper.getPageUrl(arguments);
    }

    @Test
    public void loginActionReturnsActivityIntentLoginAuthUri() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        arguments.putString("com.telenor.connect.LOGIN_AUTH_URI", "something not empty");

        Activity activity = new Activity();
        Intent intent = new Intent();
        intent.putExtras(arguments);
        activity.setIntent(intent);

        String pageUrl = ConnectUrlHelper.getPageUrl(arguments);
        assertThat(pageUrl, is("something not empty"));
    }

    @Test(expected = IllegalStateException.class)
    public void actionArgumentNullThrows() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                null);

        ConnectUrlHelper.getPageUrl(arguments);
    }

    @Test(expected = IllegalStateException.class)
    public void actionArgumentDifferentThanLoginOrPaymentThrows() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "not login or payment");

        ConnectUrlHelper.getPageUrl(arguments);
    }

    @Test
    public void getAuthorizeUriMatchesOauthStandard() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "profile");
        parameters.put("state", "abc123def456");

        ArrayList<String> locales = new ArrayList<>();
        locales.add(Locale.ENGLISH.getLanguage());

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("connect.telenordigital.com")
                .build();

        ConnectSdkProfile profile =
                new ConnectSdkProfile(RuntimeEnvironment.application, false, false);
        profile.setClientId("client-id-example");
        profile.setRedirectUri("redirect-url://here");
        ConnectTestHelper.setSdkProfile(profile);
        Uri authorizeUri = profile.getAuthorizeUri(new ParametersHolder(parameters), locales);

        Uri expected
                = Uri.parse("https://connect.telenordigital.com/oauth/authorize" +
                "?ui_locales=en" +
                "&scope=profile" +
                "&response_type=code" +
                "&redirect_uri=redirect-url%3A%2F%2Fhere" +
                "&state=abc123def456" +
                "&client_id=client-id-example");

        Set<String> expectedQueryParameterNames = expected.getQueryParameterNames();

        for (String query : expectedQueryParameterNames){
            assertThat(
                    authorizeUri.getQueryParameter(query), is(expected.getQueryParameter(query)));
        }

        assertThat(authorizeUri.getScheme(), is(expected.getScheme()));
        assertThat(authorizeUri.getAuthority(), is(expected.getAuthority()));
        assertThat(authorizeUri.getPath(), is(expected.getPath()));
    }
}
