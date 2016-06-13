package com.telenor.connect.id;

import android.app.Activity;
import android.content.Intent;

import com.telenor.connect.ConnectSdk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectSdk.class, ConnectIdService.class})
public class ParseTokenCallbackTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    @Test
    public void onErrorCallsFinishesActivityWithCanceled() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);

        Activity activity = mock(Activity.class);
        ParseTokenCallback callback = new ParseTokenCallback(activity);
        Map<String, String> errorData = new HashMap<>();
        errorData.put("something", "something else");

        callback.onError(errorData);

        verify(activity).setResult(eq(Activity.RESULT_CANCELED), argThat(new ArgumentMatcher<Intent>() {
            @Override
            public boolean matches(Object argument) {
                return ((Intent) argument).getStringExtra("something").equals("something else");
            }
        }));
        verify(activity).finish();
    }

    @Test
    public void unConfidentialOnSuccessCallsGetAccessTokenFromCode() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);
        given(ConnectSdk.isConfidentialClient()).willReturn(false);
        doNothing().when(ConnectSdk.class);
        ConnectSdk.getAccessTokenFromCode(anyString(), isA(ActivityFinisherConnectCallback.class));

        Activity activity = mock(Activity.class);
        ParseTokenCallback callback = new ParseTokenCallback(activity);
        Map<String, String> successData = new HashMap<>();

        callback.onSuccess(successData);

        verifyStatic();
        ConnectSdk.getAccessTokenFromCode(anyString(), isA(ActivityFinisherConnectCallback.class));
    }

    @Test
    public void confidentialOnSuccessFinishesWithResultOk() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);
        given(ConnectSdk.isConfidentialClient()).willReturn(true);

        Activity activity = mock(Activity.class);
        ParseTokenCallback callback = new ParseTokenCallback(activity);
        Map<String, String> successData = new HashMap<>();
        successData.put("something", "something else");

        callback.onSuccess(successData);

        verify(activity).setResult(eq(Activity.RESULT_OK), argThat(new ArgumentMatcher<Intent>() {
            @Override
            public boolean matches(Object argument) {
                return ((Intent) argument).getStringExtra("something").equals("something else");
            }
        }));
        verify(activity).finish();
    }
}
