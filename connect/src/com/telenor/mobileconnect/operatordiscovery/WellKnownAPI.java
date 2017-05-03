package com.telenor.mobileconnect.operatordiscovery;

import com.google.gson.annotations.SerializedName;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

public interface WellKnownAPI {

    @Headers("Content-Type: application/json")
    @GET("/.well-known/openid-configuration")
    WellKnownResult getWellKnownConfig();

    class WellKnownResult {
        @SerializedName("issuer")
        private String issuer;

        public String getIssuer() {
            return issuer;
        }
    }
}
