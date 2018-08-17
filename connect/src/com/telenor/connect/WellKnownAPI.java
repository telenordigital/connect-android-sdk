package com.telenor.connect;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface WellKnownAPI {

    @Headers("Content-Type: application/json")
    @GET("/oauth/.well-known/openid-configuration")
    Call<WellKnownConfig> getWellKnownConfig();

    class WellKnownConfig implements Parcelable {

        @SerializedName("issuer")
        private String issuer;
        public String getIssuer() {
            return issuer;
        }

        @SerializedName("network_authentication_target_ips")
        private Set<String> networkAuthenticationTargetIps;
        public Set<String> getNetworkAuthenticationTargetIps() {
            return networkAuthenticationTargetIps != null
                    ? networkAuthenticationTargetIps
                    : Collections.<String>emptySet();
        }

        @SerializedName("network_authentication_target_urls")
        private Set<String> networkAuthenticationTargetUrls;
        public Set<String> getNetworkAuthenticationTargetUrls() {
            return networkAuthenticationTargetUrls != null
                    ? networkAuthenticationTargetUrls
                    : Collections.<String>emptySet();
        }

        protected WellKnownConfig(Parcel in) {
            issuer = in.readString();
            int ipsCount = in.readInt();
            networkAuthenticationTargetIps = new HashSet<>(ipsCount);
            for (int i = 0; i < ipsCount; i++) {
                networkAuthenticationTargetIps.add(in.readString());
            }
            int urlsCount = in.readInt();
            networkAuthenticationTargetUrls = new HashSet<>(urlsCount);
            for (int i = 0; i < urlsCount; i++) {
                networkAuthenticationTargetUrls.add(in.readString());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(issuer);
            if (networkAuthenticationTargetIps == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(networkAuthenticationTargetIps.size());
                for (String ip : networkAuthenticationTargetIps) {
                    dest.writeString(ip);
                }
            }
            if (networkAuthenticationTargetUrls == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(networkAuthenticationTargetUrls.size());
                for (String url : networkAuthenticationTargetUrls) {
                    dest.writeString(url);
                }
            }
        }

        public static final Creator<WellKnownConfig> CREATOR = new Creator<WellKnownConfig>() {
            @Override
            public WellKnownConfig createFromParcel(Parcel in) {
                return new WellKnownConfig(in);
            }

            @Override
            public WellKnownConfig[] newArray(int size) {
                return new WellKnownConfig[size];
            }
        };

        @SerializedName("telenordigital_sdk_analytics_endpoint")
        private String analyticsEndpoint;
        public String getAnalyticsEndpoint() {
            if (analyticsEndpoint != null) {
                try {
                    if ('/' != analyticsEndpoint.charAt(analyticsEndpoint.length() - 1)) {
                        return analyticsEndpoint + "/";
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
            return analyticsEndpoint;
        }
    }
}
