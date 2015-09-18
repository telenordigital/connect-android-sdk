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
}
