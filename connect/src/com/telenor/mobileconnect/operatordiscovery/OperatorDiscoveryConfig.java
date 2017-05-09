package com.telenor.mobileconnect.operatordiscovery;

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

    public static final class Builder {
        private OperatorDiscoveryConfig operatorDiscoveryConfig;
        private int flags = 0b0000;

        public Builder() {
            operatorDiscoveryConfig = new OperatorDiscoveryConfig();
        }

        public Builder endpoint(String operatorDiscoveryEndpoint) {
            operatorDiscoveryConfig.operatorDiscoveryEndpoint = operatorDiscoveryEndpoint;
            flags |= 0b0001;
            return this;
        }
        public Builder clientId(String operatorDiscoveryClientId) {
            operatorDiscoveryConfig.operatorDiscoveryClientId = operatorDiscoveryClientId;
            flags |= 0b0010;
            return this;
        }
        public Builder clientSecret(String operatorDiscoveryClientSecret) {
            operatorDiscoveryConfig.operatorDiscoveryClientSecret = operatorDiscoveryClientSecret;
            flags |= 0b0100;
            return this;
        }
        public Builder redirectUri(String operatorDiscoveryRedirectUri) {
            operatorDiscoveryConfig.operatorDiscoveryRedirectUri = operatorDiscoveryRedirectUri;
            flags |= 0b1000;
            return this;
        }
        public OperatorDiscoveryConfig build() {
            if (flags != 0b1111) {
                throw new RuntimeException("Incomplete operator discover config");
            }
            return operatorDiscoveryConfig;
        }
    }
}
