package com.telenor.connect.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.BDDMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectSdk.class, LocalBroadcastManager.class})
public class ConnectUtilsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Test
    public void erroneousCallbackUrlCallsCallbackOnErrorWithMap() {
        mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);

        String state = "somestate";
        final String value1 = "something";
        final String value2 = "something else";
        String uri = new Uri.Builder()
                .appendQueryParameter("state", state)
                .appendQueryParameter("error", value1)
                .appendQueryParameter("error_description", value2)
                .build()
                .toString();

        ConnectCallback mock = mock(ConnectCallback.class);

        ConnectUtils.parseAuthCode(uri, state, mock);
        verify(mock).onError(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object argument) {
                Map<String, String> stringMap = (Map<String, String>) argument;
                return stringMap.get("error").equals(value1)
                        && stringMap.get("error_description").equals(value2);
            }
        }));
    }

    @Test
    public void mutatedStateInCallbackUrlCallsCallbackOnErrorWithMap() {
        mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);

        String state = "somestate";
        String uri = new Uri.Builder()
                .appendQueryParameter("state", state + "mutation")
                .build()
                .toString();

        ConnectCallback mock = mock(ConnectCallback.class);

        ConnectUtils.parseAuthCode(uri, state, mock);
        verify(mock).onError(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object argument) {
                Map<String, String> stringMap = (Map<String, String>) argument;
                return stringMap.get("error").equals("state_changed");
            }
        }));
    }

    @Test
    public void successfulCallbackUrlCallsCallbackOnSuccessWithMap() {
        mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);

        final String value1 = "something";
        final String value2 = "something else";
        String uri = new Uri.Builder()
                .appendQueryParameter("code", value1)
                .appendQueryParameter("state", value2)
                .build()
                .toString();

        ConnectCallback mock = mock(ConnectCallback.class);

        ConnectUtils.parseAuthCode(uri, value2, mock);
        verify(mock).onSuccess(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object argument) {
                Map<String, String> stringMap = (Map<String, String>) argument;
                return stringMap.get("code").equals(value1)
                        && stringMap.get("state").equals(value2);
            }
        }));
    }

    @Test
    public void sendTokenStateChangedBroadcastsIntent() {
        mockStatic(ConnectSdk.class);
        BDDMockito.given(ConnectSdk.isInitialized()).willReturn(true);
        Context context = mock(Context.class);
        BDDMockito.given(ConnectSdk.getContext()).willReturn(context);

        mockStatic(LocalBroadcastManager.class);
        LocalBroadcastManager localBroadcastManager = mock(LocalBroadcastManager.class);
        BDDMockito
                .given(LocalBroadcastManager.getInstance(context))
                .willReturn(localBroadcastManager);

        final boolean newState = true;
        ConnectUtils.sendTokenStateChanged(newState);

        verify(localBroadcastManager).sendBroadcast(argThat(new ArgumentMatcher<Intent>() {
            @Override
            public boolean matches(Object argument) {
                return ((Intent) argument)
                        .getBooleanExtra("com.telenor.connect.LOGIN_STATE", false)
                        == newState;
            }
        }));
    }
}
