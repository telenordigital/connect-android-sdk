package com.telenor.connect.id;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectSdk.class, ConnectIdService.class})
public class ParseTokenCallbackTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Test
    public void onErrorCallsCallback() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);

        ConnectCallback connectCallback = mock(ConnectCallback.class);
        ParseTokenCallback parseTokenCallback = new ParseTokenCallback(connectCallback);
        Map<String, String> errorData = new HashMap<>();
        errorData.put("something", "something else");

        parseTokenCallback.onError(errorData);
        verify(connectCallback).onError(errorData);
    }

    @Test
    public void unConfidentialOnSuccessCallsGetAccessTokenFromCode() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);
        given(ConnectSdk.isConfidentialClient()).willReturn(false);
        doNothing().when(ConnectSdk.class);
        ConnectSdk.getAccessTokenFromCode(anyString(), isA(ConnectCallback.class));

        ConnectCallback connectCallback = mock(ConnectCallback.class);
        ParseTokenCallback callback = new ParseTokenCallback(connectCallback);
        Map<String, String> successData = new HashMap<>();

        callback.onSuccess(successData);

        verifyStatic();
        ConnectSdk.getAccessTokenFromCode(anyString(), isA(ConnectCallback.class));
    }

    @Test
    public void confidentialOnSuccessFinishesWithResultOk() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);
        given(ConnectSdk.isConfidentialClient()).willReturn(true);

        ConnectCallback connectCallback = mock(ConnectCallback.class);
        ParseTokenCallback parseTokenCallback = new ParseTokenCallback(connectCallback);
        Map<String, String> successData = new HashMap<>();
        successData.put("something", "something else");

        parseTokenCallback.onSuccess(successData);
        verify(connectCallback).onSuccess(successData);
    }
}
