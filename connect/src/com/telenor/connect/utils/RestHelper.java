package com.telenor.connect.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.telenor.connect.id.ConnectAPI;
import com.telenor.connect.id.IdToken;
import com.telenor.connect.id.IdTokenDeserializer;
import com.telenor.mobileconnect.id.MobileConnectAPI;
import com.telenor.mobileconnect.operatordiscovery.OperatorDiscoveryAPI;
import com.telenor.connect.WellKnownAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class RestHelper {

    private static Map<String, ConnectAPI> connectApiMap = new HashMap<>();
    private static Map<String, MobileConnectAPI> mobileConnectApiMap = new HashMap<>();
    private static Map<String, WellKnownAPI> wellKnownApiMap = new HashMap<>();
    private static Map<String, OperatorDiscoveryAPI> operatorDiscoveryApiMap = new HashMap<>();

    public static ConnectAPI getConnectApi(String endpoint) {
        return getApi(connectApiMap, endpoint, ConnectAPI.class);
    }

    public static MobileConnectAPI getMobileConnectApi(String endpoint) {
        return getApi(mobileConnectApiMap, endpoint, MobileConnectAPI.class);
    }

    public static WellKnownAPI getWellKnownApi(String endpoint) {
        return getApi(wellKnownApiMap, endpoint, WellKnownAPI.class);
    }

    public static OperatorDiscoveryAPI getOperatorDiscoveryApi(String endpoint) {
        return getApi(operatorDiscoveryApiMap, endpoint, OperatorDiscoveryAPI.class);
    }

    private static synchronized <T> T getApi(Map<String, T> map, String endpoint, Class<T> type) {
        T api = map.get(endpoint);
        if (api == null) {
            api = buildApi(endpoint).create(type);
            map.put(endpoint, api);
        }
        return api;
    }

    private static RestAdapter buildApi(String endpoint) {
        final OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(IdToken.class, new IdTokenDeserializer())
                .create();

        final RequestInterceptor connectRetroFitInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        return new RestAdapter.Builder()
                .setClient(new OkClient(httpClient))
                .setEndpoint(endpoint)
                .setRequestInterceptor(connectRetroFitInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }
}
