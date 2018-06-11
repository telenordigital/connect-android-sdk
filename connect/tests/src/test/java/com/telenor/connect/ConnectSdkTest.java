package com.telenor.connect;

import android.content.Intent;
import android.net.Uri;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.id.ConnectStore;
import com.telenor.connect.utils.ConnectUrlHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
@PrepareForTest({ConnectIdService.class})
public class ConnectSdkTest {

    @Before
    public void before() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
    }

    @Test
    public void getsClientIdFromApplicationInfoMetaData() {
        assertThat(ConnectSdk.getClientId(), is("connect-tests"));
    }

    @Test
    public void getsRedirectUriFromApplicationInfoMetaData() {
        assertThat(ConnectSdk.getRedirectUri(), is("connect-tests://oauth2callback"));
    }

    @Test
    public void hasValidRedirectUrlCallReturnsTrueOnRedirectLink() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("scope", "anything");
        ConnectUrlHelper.getAuthorizeUri(parameters, null);

        Intent intent = new Intent();
        String savedState = new ConnectStore(RuntimeEnvironment.application).getSessionStateParam();
        intent.setData(Uri.parse("connect-tests://oauth2callback?state=" + savedState + "&code=abc"));

        assertThat(ConnectSdk.hasValidRedirectUrlCall(intent), is(true));
    }

    @Test
    public void hasValidRedirectUrlCallReturnsFalseOnNotRedirectLink() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectUrlHelper.getAuthorizeUri(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("something-not-registed://oauth2callback?state=xyz&code=abc"));

        assertThat(ConnectSdk.hasValidRedirectUrlCall(intent), is(false));
    }

    @Test
    public void hasValidRedirectUrlCallReturnsFalseOnWrongState() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectUrlHelper.getAuthorizeUri(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("something-not-registed://oauth2callback?state=NNN&code=abc"));

        assertThat(ConnectSdk.hasValidRedirectUrlCall(intent), is(false));
    }

    @Test
    public void hasValidRedirectUrlCallReturnsFalseOnMissingCode() {
        ConnectSdk.sdkInitialize(RuntimeEnvironment.application);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("state", "xyz");
        parameters.put("scope", "anything");
        ConnectUrlHelper.getAuthorizeUri(parameters, null);

        Intent intent = new Intent();
        intent.setData(Uri.parse("something-not-registed://oauth2callback?state=xyz"));

        assertThat(ConnectSdk.hasValidRedirectUrlCall(intent), is(false));
    }
}