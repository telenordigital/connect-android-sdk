package com.telenor.connect.utils;

import com.telenor.connect.ConnectSdk;

public class Validator {
    public static void notNull(Object arg, String name) {
        if (arg == null) {
            throw new NullPointerException("Argument '" + name + "' cannot be null");
        }
    }

    public static void notNullOrEmpty(String arg, String name) {
        if (arg == null || arg.length() == 0) {
            throw new IllegalArgumentException("Argument '" + name + "' cannot be null or empty");
        }
    }

    public static void SdkInitialized() {
        if (!ConnectSdk.isInitialized()) {
            throw new ConnectNotInitializedError("The SDK was not initialized, call " +
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
