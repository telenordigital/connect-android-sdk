package com.telenor.connect;

public interface ConnectCallback {
    void onSuccess(Object successData);
    void onError(Object errorData);
}
