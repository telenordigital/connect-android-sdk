package com.telenor.connect.id;

public enum IdProvider {
    CONNECT_ID("CONNECT",
            "connect.telenordigital.com",
            "connect.staging.telenordigital.com",
            null,
            null),
    TELENOR_ID("Telenor ID",
            "signin.telenorid.com",
            "signin.telenorid-staging.com",
            "manage.telenorid.com",
            "manage.telenorid-staging.com"),
    GP_ID("GP ID",
            "signin.gp-id.com",
            "signin.gp-id-staging.com",
    "manage.gp-id.com",
    "manage.gp-id-staging.com");

    private final String name;
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

    public String getName() {
        return this.name;
    }

    IdProvider(String name,
               String productionUrl,
               String stagingUrl,
               String productionSelfServiceUrl,
               String stagingSelfServiceUrl
    ) {
        this.name = name;
        this.productionUrl = productionUrl;
        this.stagingUrl = stagingUrl;
        this.productionSelfServiceUrl = productionSelfServiceUrl;
        this.stagingSelfServiceUrl = stagingSelfServiceUrl;
    }
}
