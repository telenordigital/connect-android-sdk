package com.telenor.mobileconnect.operatordiscovery;

import com.google.gson.annotations.SerializedName;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

public interface OperatorDiscoveryAPI {

    @Headers("Content-Type: application/json")
    @GET("/")
    OperatorDiscoveryResult getOperatorDiscoveryResult_ForMccMnc(
            @Header("Authorization") String auth,
            @Query("Redirect_URL") String redirectUrl,
            @Query("Identified-MCC") String identifiedMcc,
            @Query("Identified-MNC") String identifiedMnc);


    public static class BodyForMsisdn {
        @SerializedName("Redirect_URL")
        String redirectUri;

        @SerializedName("MSISDN")
        String msisdn;

        public BodyForMsisdn(String redirectUri, String msisdn) {
            this.redirectUri = redirectUri;
            this.msisdn = msisdn;
        }
    }

    @Headers("Content-Type: application/json")
    @POST("/")
    OperatorDiscoveryResult getOperatorDiscoveryResult_ForMsisdn(
            @Header("Authorization") String auth,
            @Body BodyForMsisdn body);
}
