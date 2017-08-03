/**
 * Copyright (C) 2017, TomTom International BV (http://www.tomtom.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomtom.cloud.recycling.azure.client;

/**
 * Configuration class for azure rest api calls.
 */
public class AzureRestConfig {

    private final String authority;

    private final String clientId;

    private final String clientSecret;

    private final String subscriptionId;

    private final String resourceGroupName;

    private final String appGatewayName;

    private final String apiVersion;

    private final String resource;

    private final String health;

    private AzureRestConfig(final String authority,
                            final String clientId,
                            final String clientSecret,
                            final String subscriptionId,
                            final String resourceGroupName,
                            final String appGatewayName,
                            final String apiVersion,
                            final String resource,
                            final String health) {
        this.authority = authority;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.subscriptionId = subscriptionId;
        this.resourceGroupName = resourceGroupName;
        this.appGatewayName = appGatewayName;
        this.apiVersion = apiVersion;
        this.resource = resource;
        this.health = health;
    }

    public String getAuthority() {
        return authority;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public String getAppGatewayName() {
        return appGatewayName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getResource() {
        return resource;
    }

    public String getHealth() {
        return health;
    }

    public static AzureRestConfigBuilder builder() {
        return new AzureRestConfigBuilder();
    }

    public static class AzureRestConfigBuilder {
        private String authority;

        private String clientId;

        private String clientSecret;

        private String subscriptionId;

        private String resourceGroupName;

        private String appGatewayName;

        private String apiVersion;

        private String resource;

        private String health;

        public AzureRestConfigBuilder authority(final String authority) {
            this.authority = authority;
            return this;
        }

        public AzureRestConfigBuilder clientId(final String clientId) {
            this.clientId = clientId;
            return this;
        }

        public AzureRestConfigBuilder clientSecret(final String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public AzureRestConfigBuilder subscriptionId(final String subscriptionId) {
            this.subscriptionId = subscriptionId;
            return this;
        }

        public AzureRestConfigBuilder resourceGroupName(final String resourceGroupName) {
            this.resourceGroupName = resourceGroupName;
            return this;
        }

        public AzureRestConfigBuilder appGatewayName(final String appGatewayName) {
            this.appGatewayName = appGatewayName;
            return this;
        }

        public AzureRestConfigBuilder apiVersion(final String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public AzureRestConfigBuilder resource(final String resource) {
            this.resource = resource;
            return this;
        }

        public AzureRestConfigBuilder health(final String health) {
            this.health = health;
            return this;
        }

        public AzureRestConfig build() {
            return new AzureRestConfig(
                    authority,
                    clientId,
                    clientSecret,
                    subscriptionId,
                    resourceGroupName,
                    appGatewayName,
                    apiVersion,
                    resource,
                    health);
        }
    }

}


