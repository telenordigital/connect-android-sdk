package com.telenor.connect.sms;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class SmsBroadcastReceiverTest {

    @Test
    public void onReceiveSmsCallsHandleSmsReceivedWithAddressAndBody() {
        SmsHandler mockClient = Mockito.mock(SmsHandler.class);

        Intent mockIntent = Mockito.mock(Intent.class);
        Mockito.when(mockIntent.getAction()).thenReturn(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        Bundle mockBundle = Mockito.mock(Bundle.class);
        Object[] value =
            hexStringToByteArray("0791448720003023240DD0E474D81C0EBB010000111011315214000BE474D81C0EBB5DE3771B");
        Mockito.when(mockBundle.get("pdus")).thenReturn(value);
        Mockito.when(mockIntent.getExtras()).thenReturn(mockBundle);

        SmsBroadcastReceiver smsBroadcastReceiver = new SmsBroadcastReceiver(mockClient);
        smsBroadcastReceiver.onReceive(null, mockIntent);

        Mockito.verify(mockClient).receivedSms("diafaan", "diafaan.com");
    }

    private static byte[][] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return new byte[][]{data};
    }

}
