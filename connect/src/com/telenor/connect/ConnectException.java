package com.telenor.connect;

public class ConnectException extends RuntimeException{
    static final long serialVersionUID = 1;

    public ConnectException() {
        super();
    }

    public ConnectException(String message) {
        super(message);
    }
}
