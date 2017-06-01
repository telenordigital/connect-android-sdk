package com.telenor.connect.ui;

import android.app.Activity;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;

import com.telenor.connect.ConnectCallback;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.SdkProfile;
import com.telenor.connect.id.ParseTokenCallback;
import com.telenor.connect.sms.SmsBroadcastReceiver;
import com.telenor.connect.utils.ConnectUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ConnectSdk.class, ConnectUtils.class})
public class ConnectWebViewClientTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock

    private IntentFilter smsReceivedFilter
            = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

    @Test
    public void checkForInstructionsIsCalledOnAnyTelenorDigitalHttpsPage() throws Exception {
        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);
        connectWebViewClient.onPageFinished(webView, "https://any.telenordigital.com/something");

        String expected = "javascript:if (document.getElementById('android-instructions') !== null) {" +
                "window.AndroidInterface.processInstructions(document.getElementById('android-instructions').innerHTML)" +
                "}";

        verify(webView).loadUrl(expected);
    }

    @Test
    public void checkForInstructionsIsNotCalledOnNonHttpsPages() throws Exception {
        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);
        connectWebViewClient.onPageFinished(webView, "http://any.telenordigital.com/something");

        verify(webView, never()).loadUrl("javascript:window.AndroidInterface" +
                ".processInstructions(document.getElementById('android-instructions').innerHTML);");
    }

    @Test
    public void checkForInstructionsIsNotCalledOnNonTelenorDigitalPages() throws Exception {
        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);
        connectWebViewClient.onPageFinished(webView, "https://any.telenordigital.com.fish.biz/foo");

        verify(webView, never()).loadUrl("javascript:window.AndroidInterface" +
                ".processInstructions(document.getElementById('android-instructions').innerHTML);");
    }

    @Test
    public void activityRegisterReceiverWithSmsReceivedFilterIsCalledOnPinInstruction()
            throws Exception {
        ConnectCallback callback = mock(ConnectCallback.class);
        Instruction instruction = getPinInstruction();

        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);
        List<Instruction> instructions = Collections.singletonList(instruction);
        connectWebViewClient.givenInstructions(instructions);

        verify(activity)
                .registerReceiver(
                        any(SmsBroadcastReceiver.class),
                        argThat(new IntentActionMatcher<>(smsReceivedFilter)));
    }

    @NonNull
    private Instruction getPinInstruction() {
        Instruction instruction = new Instruction();
        instruction.setName(Instruction.PIN_INSTRUCTION_NAME);
        return instruction;
    }

    private class IntentActionMatcher<T> extends ArgumentMatcher<T> {
        T thisObject;

        public IntentActionMatcher(T thisObject) {
            this.thisObject = thisObject;
        }

        @Override
        public boolean matches(Object argument) {
            return ((IntentFilter) argument)
                    .getAction(0).equals(((IntentFilter) thisObject).getAction(0));
        }
    }

    @Test
    public void urlsThatDoNotStartWithPaymentCancelOrSuccessOrRedirectUriDoesNotOverrideLoading() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);

        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);

        boolean result = connectWebViewClient.shouldOverrideUrlLoading(
                webView,
                "something not payment or redirect");

        assertThat(result, is(false));
    }

    @Test
    public void urlsThatStartWithPaymentSuccessUriFinishesActivityWithOk() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);
        given(ConnectSdk.getPaymentSuccessUri()).willReturn("success-uri");

        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);

        boolean result = connectWebViewClient.shouldOverrideUrlLoading(
                webView,
                "success-uri");

        assertThat(result, is(true));
        verify(activity).setResult(Activity.RESULT_OK);
        verify(activity).finish();
    }

    @Test
    public void urlsThatStartWithPaymentCancelUriFinishesActivityWithCancel() {
        mockStatic(ConnectSdk.class);
        given(ConnectSdk.isInitialized()).willReturn(true);
        given(ConnectSdk.getPaymentCancelUri()).willReturn("cancel-uri");

        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);

        boolean result = connectWebViewClient.shouldOverrideUrlLoading(
                webView,
                "cancel-uri");

        assertThat(result, is(true));
        verify(activity).setResult(Activity.RESULT_CANCELED);
        verify(activity).finish();
    }

    @Test
    public void urlsThatStartWithRedirectUriCallsParseAuthCode() {
        SdkProfile sdkProfileMock = mock(SdkProfile.class);
        when(sdkProfileMock.isInitialized()).thenReturn(true);

        mockStatic(ConnectSdk.class);
        given(ConnectSdk.getSdkProfile()).willReturn(sdkProfileMock);
        given(ConnectSdk.getRedirectUri()).willReturn("redirect-uri");

        mockStatic(ConnectUtils.class);
        doNothing().when(ConnectUtils.class);
        ConnectUtils.parseAuthCode(eq("redirect-uri"), any(ParseTokenCallback.class));

        ConnectCallback callback = mock(ConnectCallback.class);
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView, callback);

        boolean result = connectWebViewClient.shouldOverrideUrlLoading(
                webView,
                "redirect-uri");

        assertThat(result, is(true));
        verifyStatic();
        ConnectUtils.parseAuthCode(eq("redirect-uri"), any(ParseTokenCallback.class));
    }
}
