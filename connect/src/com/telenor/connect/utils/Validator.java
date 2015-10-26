package com.telenor.connect.utils;

import com.telenor.connect.ConnectException;
import com.telenor.connect.ConnectNotInitializedException;
import com.telenor.connect.ConnectSdk;
import com.telenor.connect.id.ConnectTokens;
import com.telenor.connect.id.IdToken;

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

    public static void SdkInitialized() {
        if (!ConnectSdk.isInitialized()) {
            throw new ConnectNotInitializedException("The SDK was not initialized, call " +
                    "ConnectSdk.sdkInitialize() first");
        }
    }

    public static void ValidateAuthenticationState(String state) {
        if (ConnectSdk.getLastAuthenticationState() != null &&
                !ConnectSdk.getLastAuthenticationState().isEmpty() &&
                !ConnectSdk.getLastAuthenticationState().equals(state)) {
            throw new ConnectException("The state parameter was changed between authentication " +
                    "and now.");
        }
    }

    public static void ValidateIdToken(IdToken token) {
        //TODO Actually validate the JWT token.
    }

    public static void ValidateTokens(ConnectTokens tokens) {
        notNullOrEmpty(tokens.accessToken, "access_token");
        notNull(tokens.expiresIn, "expires_in");
        notNullOrEmpty(tokens.refreshToken, "refresh_token");
        notNullOrEmpty(tokens.scope, "scope");
        notNullOrEmpty(tokens.tokenType, "token_type");

        if (tokens.idToken != null) {
            ValidateIdToken(tokens.idToken);
        }
    }
}
