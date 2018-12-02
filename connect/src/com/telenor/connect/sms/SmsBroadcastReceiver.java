package com.telenor.connect.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private final SmsHandler smsHandler;

    /**
     * @param smsHandler the {@link SmsHandler} that will called upon when new SMS arrive.
     */
    public SmsBroadcastReceiver(SmsHandler smsHandler) {
        this.smsHandler = smsHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
        if (status == null) {
            return;
        }

        switch (status.getStatusCode()) {
            case CommonStatusCodes.SUCCESS:
                String body = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                smsHandler.receivedSms(body);
                return;
            case CommonStatusCodes.TIMEOUT:
                SmsRetrieverUtil.startSmsRetriever(context);
                return;
            default:
        }
    }
}
