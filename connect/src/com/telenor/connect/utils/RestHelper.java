package com.telenor.connect.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit.Ok3Client;
import com.telenor.connect.AnalyticsAPI;
import com.telenor.connect.BuildConfig;
import com.telenor.connect.WellKnownAPI;
import com.telenor.connect.id.ConnectAPI;
import com.telenor.connect.id.IdToken;
import com.telenor.connect.id.IdTokenDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class RestHelper {

    private static Map<String, ConnectAPI> connectApiMap = new HashMap<>();
    private static Map<String, WellKnownAPI> wellKnownApiMap = new HashMap<>();
    private static Map<String, AnalyticsAPI> analyticsApiMap = new HashMap<>();

    public static ConnectAPI getConnectApi(String endpoint) {
        return getApi(connectApiMap, endpoint, ConnectAPI.class);
    }

    public static WellKnownAPI getWellKnownApi(String endpoint) {
        return getApi(wellKnownApiMap, endpoint, WellKnownAPI.class);
    }

    public static AnalyticsAPI getAnalyticsApi(String endpoint) {
        return getApi(analyticsApiMap, endpoint, AnalyticsAPI.class);
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
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

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
                .setClient(new Ok3Client(httpClient))
                .setEndpoint(endpoint)
                .setRequestInterceptor(connectRetroFitInterceptor)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(new GsonConverter(gson))
                .build();
    }
}
