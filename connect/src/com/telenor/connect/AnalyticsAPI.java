package com.telenor.connect;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AnalyticsAPI {
    class SDKAnalyticsData {
        @SerializedName("appName")
        private final String appName;

        @SerializedName("appVersion")
        private final String appVersion;

        @SerializedName("subject")
        private final String subject;

        @SerializedName("log_session_id")
        private final String logSessionId;

        @SerializedName("advertisingId")
        private final String advertisingId;

        @SerializedName("tsSdkInitiliazation")
        private final long tsSdkInitiliazation;

        @SerializedName("tsLoginButtonClicked")
        private final long tsLoginButtonClicked;

        @SerializedName("tsRedirectUrlInvoked")
        private final long tsRedirectUrlInvoked;

        @SerializedName("tsTokenResponseReceived")
        private final long tsTokenResponseReceived;

        @SerializedName("deviceManufacturer")
        private final String deviceManufacturer;

        @SerializedName("deviceModel")
        private final String deviceModel;

        @SerializedName("osName")
        private final String osName;

        @SerializedName("osVersion")
        private final String osVersion;

        @SerializedName("os_sdk_version")
        private final int osSdkVersion;

        public SDKAnalyticsData(
                final String appName,
                final String appVersion,
                final String subject,
                final String logSessionId,
                final String advertisingId,
                final long tsSdkInitiliazation,
                final long tsLoginButtonClicked,
                final long tsRedirectUrlInvoked,
                final long tsTokenResponseReceived) {
            this.appName = appName;
            this.appVersion = appVersion;
            this.subject = subject;
            this.logSessionId = logSessionId;
            this.advertisingId = advertisingId;
            this.tsSdkInitiliazation = tsSdkInitiliazation;
            this.tsLoginButtonClicked = tsLoginButtonClicked;
            this.tsRedirectUrlInvoked = tsRedirectUrlInvoked;
            this.tsTokenResponseReceived = tsTokenResponseReceived;
            this.deviceManufacturer = Build.MANUFACTURER;
            this.deviceModel = Build.MODEL;
            this.osName = System.getProperty("os.name");
            this.osVersion = System.getProperty("os.version");
            this.osSdkVersion = Build.VERSION.SDK_INT;
        }
    }

    @Headers("Content-Type: application/json")
    @POST("V1/android")
    Call<Void> sendAnalyticsData(
            @Header("Authorization") String auth,
            @Body SDKAnalyticsData analyticsData);
}
