package com.telenor.mobileconnect.operatordiscovery;

import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.ConnectException;
import com.telenor.connect.WellKnownAPI;

import java.net.MalformedURLException;
import java.net.URL;
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


    class BodyForMsisdn {
        @SerializedName("Redirect_URL")
        private String redirectUri;

        @SerializedName("MSISDN")
        private String msisdn;

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


    class OperatorDiscoveryResult {

        @SerializedName("subscriber_id")
        private String subscriberId;

        @SerializedName("ttl")
        private int ttl;

        @SerializedName("response")
        private OperatorInfo operatorInfo;

        private static class OperatorInfo {
            @SerializedName("country")
            private String country;

            @SerializedName("currency")
            private String currency;

            @SerializedName("client_id")
            private String clientId;

            @SerializedName("client_name")
            private String clientName;

            @SerializedName("client_secret")
            private String clientSecret;

            @SerializedName("serving_operator")
            private String servingOperator;

            @SerializedName("apis")
            private OperatorApi apis;

        }

        private static class OperatorApi {
            @SerializedName("operatorid")
            private OperatorIdApi operatorIdApi;
        }

        private static class OperatorIdApi {
            @SerializedName("link")
            private List<OperatorIdApiEndpoint> link;
        }

        private static class OperatorIdApiEndpoint {
            @SerializedName("rel")
            private String usage;

            @SerializedName("href")
            private String href;
        }

        public String getEndpoint(String rel) {
            for (OperatorIdApiEndpoint endpoint : operatorInfo.apis.operatorIdApi.link) {
                if (endpoint.usage.equals(rel)) {
                    return endpoint.href;
                }
            }
            return null;
        }

        public HttpUrl getMobileConnectApiUrl() {
            return HttpUrl.parse(getEndpoint("authorization"));
        }

        public String getBasePath() {
            for (OperatorIdApiEndpoint endpoint : operatorInfo.apis.operatorIdApi.link) {
                if (endpoint.usage.equals("authorization")) {
                    return endpoint.href.substring(0, endpoint.href.lastIndexOf("/"));
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

        public String getPath(String rel) {
            try {
                return new URL(getEndpoint(rel)).getPath();
            } catch (MalformedURLException e) {
                throw new ConnectException(e);
            }
        }
        public String getWellKnownEndpoint() {
            String endpoint = getEndpoint("openid-configuration");
            if (endpoint != null) {
                return endpoint;
            }
            return getBasePath() + WellKnownAPI.OPENID_CONFIGURATION_PATH;
        }
    }

    class PlainOperatorDiscoveryConfig implements OperatorDiscoveryConfig {

        private String endpoint;
        private String clientId;
        private String clientSecret;
        private String redirectUri;

        public PlainOperatorDiscoveryConfig(String endpoint, String clientId, String clientSecret, String redirectUri) {
            this.endpoint = endpoint;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
        }

        @Override
        public String getOperatorDiscoveryClientId() {
            return clientId;
        }

        @Override
        public String getOperatorDiscoveryClientSecret() {
            return clientSecret;
        }

        @Override
        public String getOperatorDiscoveryRedirectUri() {
            return redirectUri;
        }

        @Override
        public String getOperatorDiscoveryEndpoint() {
            return endpoint;
        }
    }
}
