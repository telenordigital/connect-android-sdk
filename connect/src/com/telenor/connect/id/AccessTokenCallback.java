package com.telenor.connect.id;

import retrofit2.Call;
import retrofit2.Response;

public interface AccessTokenCallback {
    void success(String accessToken);
    void unsuccessfulResult(Response response, boolean userWasCleared);
    void failure(Call<ConnectTokensTO> call, Throwable error);
    void noSignedInUser();
}
