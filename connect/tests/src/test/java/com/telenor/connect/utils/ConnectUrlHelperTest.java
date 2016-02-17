package com.telenor.connect.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

        String pageUrl = ConnectUrlHelper.getPageUrl(arguments, null);
        assertThat(pageUrl, is("some-url-argument://"));
    }

    @Test(expected = IllegalStateException.class)
    public void loginActionArgumentThrowsOnNullActivity() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        ConnectUrlHelper.getPageUrl(arguments, null);
    }

    @Test(expected = IllegalStateException.class)
    public void loginActionArgumentThrowsOnNullActivityIntent() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "com.telenor.connect.LOGIN_ACTION");

        Activity activity = new Activity();

        ConnectUrlHelper.getPageUrl(arguments, activity);
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

        ConnectUrlHelper.getPageUrl(arguments, activity);
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

        ConnectUrlHelper.getPageUrl(arguments, activity);
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

        String pageUrl = ConnectUrlHelper.getPageUrl(arguments, activity);
        assertThat(pageUrl, is("something not empty"));
    }

    @Test(expected = IllegalStateException.class)
    public void actionArgumentNullThrows() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                null);

        ConnectUrlHelper.getPageUrl(arguments, null);
    }

    @Test(expected = IllegalStateException.class)
    public void actionArgumentDifferentThanLoginOrPaymentThrows() {
        Bundle arguments = new Bundle();
        arguments.putString(
                "com.telenor.connect.ACTION_ARGUMENT",
                "not login or payment");

        ConnectUrlHelper.getPageUrl(arguments, null);
    }

}
