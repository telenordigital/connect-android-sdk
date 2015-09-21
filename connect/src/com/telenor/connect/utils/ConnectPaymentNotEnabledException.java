package com.telenor.connect.utils;

public class ConnectPaymentNotEnabledException extends RuntimeException {
    public ConnectPaymentNotEnabledException(String message) {
        super(message);
    }
}
