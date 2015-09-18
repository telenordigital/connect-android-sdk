package com.telenor.connect.utils;

public class ConnectNotInitializedError extends RuntimeException {
    public ConnectNotInitializedError(String message) {
        super(message);
    }
}
