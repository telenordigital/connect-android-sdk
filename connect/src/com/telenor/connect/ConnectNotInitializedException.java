package com.telenor.connect;

import com.telenor.connect.ConnectException;

public class ConnectNotInitializedException extends ConnectException {
    public ConnectNotInitializedException(String message) {
        super(message);
    }
}
