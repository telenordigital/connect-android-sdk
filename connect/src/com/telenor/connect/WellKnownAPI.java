package com.telenor.connect;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;

public interface WellKnownAPI {

    public static final String OPENID_CONFIGURATION_PATH = "/.well-known/openid-configuration";

    @Headers("Content-Type: application/json")
    @GET("/")
    void getWellKnownConfig(Callback<WellKnownConfig> callback);

    class WellKnownConfig implements Parcelable {

        @SerializedName("issuer")
        private String issuer;
        public String getIssuer() {
            return issuer;
        }

        @SerializedName("network_authentication_target_ips")
        private Set<String> networkAuthenticationTargetIps;
        public Set<String> getNetworkAuthenticationTargetIps() {
            return networkAuthenticationTargetIps;
        }

        protected WellKnownConfig(Parcel in) {
            issuer = in.readString();
            networkAuthenticationTargetIps = new HashSet<>();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                networkAuthenticationTargetIps.add(in.readString());
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
                return;
            }
            dest.writeInt(networkAuthenticationTargetIps.size());
            for (String ip : networkAuthenticationTargetIps) {
                dest.writeString(ip);
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
    }
}
