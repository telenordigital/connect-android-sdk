package com.telenor.connect.sms;

public interface SmsHandler {
    void receivedSms(String messageBody);
}
