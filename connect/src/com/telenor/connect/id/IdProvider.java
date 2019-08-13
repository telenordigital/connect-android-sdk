package com.telenor.connect.id;

public enum IdProvider {
    CONNECT_ID("CONNECT","connect.telenordigital.com", "connect.staging.telenordigital.com"),
    TELENOR_ID("Telenor ID", "signin.telenorid.com", "signin.staging-telenorid.com");

    private String name;
    private String productionUrl;
    private String stagingUrl;

    public String getUrl(boolean useStaging) {
        return useStaging ? stagingUrl : productionUrl;
    }

    public String getName() { return this.name; }

    IdProvider(String name, String productionUrl, String stagingUrl) {
        this.name = name;
        this.productionUrl = productionUrl;
        this.stagingUrl = stagingUrl;
    }
}
