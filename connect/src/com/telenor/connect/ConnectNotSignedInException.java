package com.telenor.connect;

public class ConnectNotSignedInException extends ConnectException {
    public ConnectNotSignedInException() {
        super();
    }

    public ConnectNotSignedInException(String message) {
        super(message);
    }

    public ConnectNotSignedInException(Throwable throwable) {
        super(throwable);
    }

    public ConnectNotSignedInException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
