package com.telenor.mobileconnect.operatordiscovery;

import org.immutables.value.Value;

public final class OperatorDiscoveryConfig {

    String operatorDiscoveryEndpoint;
    String operatorDiscoveryClientId;
    String operatorDiscoveryClientSecret;
    String operatorDiscoveryRedirectUri;

    private OperatorDiscoveryConfig() {
    }

    public String getOperatorDiscoveryEndpoint() {
        return operatorDiscoveryEndpoint;
    }

    public String getOperatorDiscoveryClientId() {
        return operatorDiscoveryClientId;
    }

    public String getOperatorDiscoveryClientSecret() {
        return operatorDiscoveryClientSecret;
    }

    public String getOperatorDiscoveryRedirectUri() {
        return operatorDiscoveryRedirectUri;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        OperatorDiscoveryConfig operatorDiscoveryConfig;

        public Builder() {
            operatorDiscoveryConfig = new OperatorDiscoveryConfig();
        }

        public Builder endpoint(String operatorDiscoveryEndpoint) {
            operatorDiscoveryConfig.operatorDiscoveryEndpoint = operatorDiscoveryEndpoint;
            return this;
        }
        public Builder clientId(String operatorDiscoveryClientId) {
            operatorDiscoveryConfig.operatorDiscoveryClientId = operatorDiscoveryClientId;
            return this;
        }
        public Builder clientSecret(String operatorDiscoveryClientSecret) {
            operatorDiscoveryConfig.operatorDiscoveryClientSecret = operatorDiscoveryClientSecret;
            return this;
        }
        public Builder redirectUri(String operatorDiscoveryRedirectUri) {
            operatorDiscoveryConfig.operatorDiscoveryRedirectUri = operatorDiscoveryRedirectUri;
            return this;
        }
        public OperatorDiscoveryConfig build() {
            return operatorDiscoveryConfig;
        }
    }
}
