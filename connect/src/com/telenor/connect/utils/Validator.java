package com.telenor.connect.utils;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.ConnectSdkProfile;
import com.telenor.connect.id.ConnectTokensTO;

import java.util.Date;

public class Validator {
    public static void notNull(Object var, String name) {
        if (var == null) {
            throw new NullPointerException("Variable '" + name + "' cannot be null");
        }
    }

    public static void notNullOrEmpty(String var, String name) {
        if (var == null || var.isEmpty()) {
            throw new IllegalArgumentException("Variable '" + name + "' cannot be null or empty");
        }
    }

    public static void notDifferent(String var1, String var2, String name) {
        if ((var1 == null && var2 != null) || (var1 != null && !var1.equals(var2))) {
            throw new ConnectException("Variable '" + name + "': unexpected value");
        }
    }

    public static void sdkInitialized() {
        if (!ConnectSdk.isInitialized()) {
            throw new ConnectNotInitializedException("The SDK was not initialized, call " +
                    "ConnectSdk.sdkInitialize() first");
        }
    }

    public static void connectIdOnly() {
        if (!(ConnectSdk.getSdkProfile() instanceof ConnectSdkProfile)) {
            throw new ConnectNotInitializedException("The SDK was either not initialized or not " +
                    "initialized in the right way; call ConnectSdk.sdkInitialize() first");
        }
    }

    public static void validateAuthenticationState(String state) {
        if (!validState(state)) {
            throw new ConnectException("The state parameter was changed between authentication " +
                    "and now.");
        }
    }

    public static boolean validState(String state) {
        return ConnectSdk.getLastAuthenticationState() == null
                || ConnectSdk.getLastAuthenticationState().isEmpty()
                || ConnectSdk.getLastAuthenticationState().equals(state);
    }

    public static void validateTokens(ConnectTokensTO tokens, Date serverTimestamp) {
        sdkInitialized();
        ConnectSdk.getSdkProfile().validateTokens(tokens, serverTimestamp);
    }
}
