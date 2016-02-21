package com.telenor.connect.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.telenor.connect.id.ConnectAPI;
import com.telenor.connect.id.IdToken;
import com.telenor.connect.id.IdTokenDeserializer;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class RestHelper {
    public static ConnectAPI getConnectApi(String endpoint) {
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
                .build()
                .create(ConnectAPI.class);
    }
}
