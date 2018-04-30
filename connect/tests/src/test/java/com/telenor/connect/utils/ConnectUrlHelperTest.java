package com.telenor.connect.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.telenor.connect.BrowserType;

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

import okhttp3.HttpUrl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class ConnectUrlHelperTest {

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
    public void actionArgumentDifferentThanLoginThrows() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "not login");

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

        Uri authorizeUri = ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                "client-id-example",
                "redirect-url://here",
                locales,
                url,
                BrowserType.WEB_VIEW)
                .buildUpon()
                .appendPath(ConnectUrlHelper.OAUTH_PATH)
                .appendPath("authorize")
                .build();

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

    @Test
    public void nullBrowserTypeOnGetAuthorizeUriReturnsNotDefinedVersionParam() {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("connect.telenordigital.com")
                .build();

        ArrayList<String> locales = new ArrayList<>();
        locales.add(Locale.ENGLISH.getLanguage());

        HashMap<String, String> parameters = new HashMap<>();
        Uri authorizeUri = ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                "client-id-example",
                "redirect-url://here",
                locales,
                url,
                null);

        assertThat(authorizeUri
                .getQueryParameter("telenordigital_sdk_version")
                .endsWith("not-defined"), is(true));
    }

    @Test
    public void chromeCustomTabBrowserTypeOnGetAuthorizeUriReturnsChromeCustomTabParam() {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("connect.telenordigital.com")
                .build();

        ArrayList<String> locales = new ArrayList<>();
        locales.add(Locale.ENGLISH.getLanguage());

        HashMap<String, String> parameters = new HashMap<>();
        Uri authorizeUri = ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                "client-id-example",
                "redirect-url://here",
                locales,
                url,
                BrowserType.CHROME_CUSTOM_TAB);

        assertThat(
                authorizeUri
                        .getQueryParameter("telenordigital_sdk_version")
                        .endsWith("chrome-custom-tab"), is(true));
    }

    @Test
    public void webViewBrowserTypeOnGetAuthorizeUriReturnsWebViewParam() {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("connect.telenordigital.com")
                .build();

        ArrayList<String> locales = new ArrayList<>();
        locales.add(Locale.ENGLISH.getLanguage());

        HashMap<String, String> parameters = new HashMap<>();
        Uri authorizeUri = ConnectUrlHelper.getAuthorizeUriStem(
                parameters,
                "client-id-example",
                "redirect-url://here",
                locales,
                url,
                BrowserType.WEB_VIEW);

        assertThat(
                authorizeUri
                        .getQueryParameter("telenordigital_sdk_version")
                        .endsWith("web-view"), is(true));
    }
}
