package com.telenor.connect.id;

public interface AccessTokenCallback {
    void onSuccess(String accessToken);
    void onError(Object errorData);
}
