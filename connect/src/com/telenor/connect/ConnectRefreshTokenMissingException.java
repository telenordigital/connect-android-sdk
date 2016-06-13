package com.telenor.connect;

public class ConnectRefreshTokenMissingException extends IllegalStateException {
    public ConnectRefreshTokenMissingException() {
        super();
    }

    public ConnectRefreshTokenMissingException(String message) {
        super(message);
    }

    public ConnectRefreshTokenMissingException(Throwable throwable) {
        super(throwable);
    }

    public ConnectRefreshTokenMissingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
