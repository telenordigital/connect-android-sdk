package com.telenor.connect;

import com.telenor.connect.ConnectException;

public class ConnectPaymentNotEnabledException extends ConnectException {
    public ConnectPaymentNotEnabledException(String message) {
        super(message);
    }
}
