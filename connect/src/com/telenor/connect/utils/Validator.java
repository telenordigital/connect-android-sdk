package com.telenor.connect.utils;

import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectPaymentNotEnabledException;
import com.telenor.connect.ConnectSdk;

public class Validator {
    public static void notNull(Object var, String name) {
        if (var == null) {
            throw new NullPointerException("Variable '" + name + "' cannot be null");
        }
    }

    public static void notNullOrEmpty(String var, String name) {
        if (var == null || var.length() == 0) {
            throw new IllegalArgumentException("Variable '" + name + "' cannot be null or empty");
        }
    }

    public static void SdkInitialized() {
        if (!ConnectSdk.isInitialized()) {
            throw new ConnectNotInitializedException("The SDK was not initialized, call " +
                    "ConnectSdk.sdkInitialize() first");
        }
    }

    public static void PaymentEnabled() {
        if (!ConnectSdk.isPaymentEnabled()) {
            throw new ConnectPaymentNotEnabledException("Connect Payment was not enabled, " +
                    "enable it by setting " + ConnectSdk.PAYMENT_ENABLED_PROPERTY + "to true in" +
                    "the application manifest.");
        }
    }

}
