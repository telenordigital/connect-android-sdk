package com.telenor.connect.id;

import com.telenor.connect.R;

public enum IdProvider {
    CONNECT_ID(R.string.brand_name_connect,
            R.string.subscribers_name_connect,
            R.string.network_name_connect,
            R.drawable.ic_telenorid_logo,
            "connect.telenordigital.com",
            "connect.staging.telenordigital.com",
            null,
            null),
    TELENOR_ID(R.string.brand_name_telenorid,
            R.string.subscribers_name_telenorid,
            R.string.network_name_telenorid,
            R.drawable.ic_telenorid_logo,
            "signin.telenorid.com",
            "signin.telenorid-staging.com",
            "manage.telenorid.com",
            "manage.telenorid-staging.com"),
    GP_ID(R.string.brand_name_gpid,
            R.string.subscribers_name_gpid,
            R.string.network_name_gpid,
            R.drawable.ic_gpid_logo,
            "signin.gp-id.com",
            "signin.gp-id-staging.com",
    "manage.gp-id.com",
    "manage.gp-id-staging.com"),
    DTAC_ID(R.string.brand_name_dtacid,
            R.string.subscribers_name_dtacid,
            R.string.network_name_dtacid,
            R.drawable.ic_dtacid_logo,
            "signin.dtac-id.com",
            "signin.dtac-id-staging.com",
            "manage.dtac-id.com",
            "manage.dtac-id-staging.com");

    private final int nameKey;
    private final int subscribersKey;
    private final int networkKey;
    private final int logoKey;

    private final String productionUrl;
    private final String stagingUrl;
    private final String productionSelfServiceUrl;
    private final String stagingSelfServiceUrl;

    public String getUrl(boolean useStaging) {
        return useStaging ? this.stagingUrl : this.productionUrl;
    }

    public String getSelfServiceUrl(boolean useStaging) {
        return useStaging ? this.stagingSelfServiceUrl : this.productionSelfServiceUrl;
    }

    public int getNameKey() {
        return this.nameKey;
    }

    public int getSubscribersKey() {
        return this.subscribersKey;
    }

    public int getNetworkKey() {
        return this.networkKey;
    }

    public int getLogoKey() {
        return this.logoKey;
    }

    IdProvider(int nameKey,
               int subscribersKey,
               int networkKey,
               int logoKey,
               String productionUrl,
               String stagingUrl,
               String productionSelfServiceUrl,
               String stagingSelfServiceUrl
    ) {
        this.nameKey = nameKey;
        this.subscribersKey = subscribersKey;
        this.networkKey = networkKey;
        this.logoKey = logoKey;
        this.productionUrl = productionUrl;
        this.stagingUrl = stagingUrl;
        this.productionSelfServiceUrl = productionSelfServiceUrl;
        this.stagingSelfServiceUrl = stagingSelfServiceUrl;
    }
}
