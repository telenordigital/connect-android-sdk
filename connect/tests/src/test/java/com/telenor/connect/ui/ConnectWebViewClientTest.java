package com.telenor.connect.ui;

import android.app.Activity;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;

import com.telenor.connect.sms.SmsBroadcastReceiver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
public class ConnectWebViewClientTest {

    private IntentFilter smsReceivedFilter
            = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

    @Test
    public void checkForInstructionsIsCalledOnEveryPage() throws Exception {
        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView);
        connectWebViewClient.onPageFinished(webView, "any");

        verify(webView).loadUrl("javascript:window.AndroidInterface" +
                ".processInstructions(document.getElementById('android-instructions').innerHTML);");
    }

    @Test
    public void activityRegisterReceiverWithSmsReceivedFilterIsCalledOnPinInstruction()
            throws Exception {
        Instruction instruction = getPinInstruction();

        Activity activity = mock(Activity.class);
        WebView webView = mock(WebView.class);
        View loadingView = mock(View.class);
        View errorView = mock(View.class);

        ConnectWebViewClient connectWebViewClient
                = new ConnectWebViewClient(activity, webView, loadingView, errorView);
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
        instruction.setConfig(new Instruction.Config("", "", ""));
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

}
