package com.telenor.connect.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.telenor.connect.AnalyticsAPI;
import com.telenor.connect.BuildConfig;
import com.telenor.connect.WellKnownAPI;
import com.telenor.connect.id.ConnectAPI;
import com.telenor.connect.id.IdToken;
import com.telenor.connect.id.IdTokenDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    private static Retrofit buildApi(String endpoint) {
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);

        final Interceptor headers = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .build());
            }
        };

        final OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(headers)
                .build();

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(IdToken.class, new IdTokenDeserializer())
                .create();

        return new Retrofit.Builder()
                .callFactory(httpClient)
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
