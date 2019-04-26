package com.telenor.connect.id;

public enum IdProvider {
    CONNECT_ID("connect.telenordigital.com", "connect.staging.telenordigital.com"),
    TELENOR_ID("example.com", "signin.staging-telenorid.com"); // TODO: production is not set yet

    private String productionUrl;
    private String stagingUrl;

    public String getUrl(boolean useStaging) {
        return useStaging ? stagingUrl : productionUrl;
    }

    IdProvider(String productionUrl, String stagingUrl) {
        this.productionUrl = productionUrl;
        this.stagingUrl = stagingUrl;
    }
}
