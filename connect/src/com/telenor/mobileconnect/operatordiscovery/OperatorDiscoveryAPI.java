package com.telenor.mobileconnect.operatordiscovery;

import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.HttpUrl;
import com.telenor.connect.ConnectException;
import com.telenor.connect.WellKnownAPI;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Query;

public interface OperatorDiscoveryAPI {

    @Headers("Content-Type: application/json")
    @GET("/")
    void getOperatorDiscoveryResult_ForMccMnc(
            @Header("Authorization") String auth,
            @Query("Redirect_URL") String redirectUrl,
            @Query("Identified-MCC") String identifiedMcc,
            @Query("Identified-MNC") String identifiedMnc,
            Callback<OperatorDiscoveryResult> callback);

    @Headers("Content-Type: application/json")
    @GET("/")
    void getOperatorSelectionResult(
            @Header("Authorization") String auth,
            @Query("Redirect_URL") String redirectUrl,
            Callback<OperatorSelectionResult> callback);

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

    class OperatorSelectionResult {

        @SerializedName("links")
        private List<OperatorDiscoveryResult.OperatorIdApiEndpoint> links;

        public String getEndpoint() {
            if (links == null) {
                return null;
            }
            for (OperatorDiscoveryResult.OperatorIdApiEndpoint endpoint : links) {
                if (endpoint.usage.equals("operatorSelection")) {
                    return endpoint.href;
                }
            }
            return null;
        }
    }
}