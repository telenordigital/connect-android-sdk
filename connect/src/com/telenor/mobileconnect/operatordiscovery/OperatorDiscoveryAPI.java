package com.telenor.mobileconnect.operatordiscovery;

import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.HttpUrl;

import java.util.List;

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


    public static class OperatorDiscoveryResult {

        @SerializedName("subscriber_id")
        private String subscriberId;

        @SerializedName("ttl")
        private int ttl;

        @SerializedName("response")
        private OperatorInfo operatorInfo;

        private static class OperatorInfo {
            @SerializedName("country")
            String country;

            @SerializedName("currency")
            String currency;

            @SerializedName("client_id")
            String clientId;

            @SerializedName("client_name")
            String clientName;

            @SerializedName("client_secret")
            String clientSecret;

            @SerializedName("serving_operator")
            String servingOperator;

            @SerializedName("apis")
            OperatorApi apis;

        }

        private static class OperatorApi {
            @SerializedName("operatorid")
            OperatorIdApi operatorIdApi;
        }

        private static class OperatorIdApi {
            @SerializedName("link")
            List<OperatorIdApiEndpoint> link;
        }

        private static class OperatorIdApiEndpoint {
            @SerializedName("rel")
            String usage;

            @SerializedName("href")
            String href;
        }

        public String getUrl(String rel) {
            for (OperatorIdApiEndpoint endpoint : operatorInfo.apis.operatorIdApi.link) {
                if (endpoint.usage.equals(rel)) {
                    return endpoint.href;
                }
            }
            return null;
        }

        public HttpUrl getMobileConnectApiUrl() {
            return HttpUrl.parse(getUrl("authorization"));
        }

        public String getBasePath() {
            for (OperatorIdApiEndpoint endpoint : operatorInfo.apis.operatorIdApi.link) {
                if (endpoint.usage.equals("authorization")) {
                    return endpoint.href.replace("/authorize", "");
                }
            }
            return null;
        }

        public String getSubscriberId() {
            return subscriberId;
        }

        public String getClientId() {
            return operatorInfo.clientId;
        }

        public String getClientSecret() {
            return operatorInfo.clientSecret;
        }

        public String getServingOperator() {
            return operatorInfo.servingOperator;
        }

    }
}
