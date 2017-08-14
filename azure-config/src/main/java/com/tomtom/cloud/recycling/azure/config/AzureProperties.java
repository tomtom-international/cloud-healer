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
package com.tomtom.cloud.recycling.azure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("active.recycling.azure")
public class AzureProperties {

    private String metricsInstrumentationKey;

    private String metricsNamespace;

    private String metricsSuffix;

    private String eventhubNamespace;

    private String eventhubName;

    private String eventhubAccessKeyname;

    private String eventhubAccessKey;

    private int timeout;

    private String instance;

    private String scaleSetId;

    private String groupName;

    private String gatewayName;

    private String subscription;

    private String apiVersion;

    private String health;

    public String getMetricsInstrumentationKey() {
        return metricsInstrumentationKey;
    }

    public void setMetricsInstrumentationKey(String metricsInstrumentationKey) {
        this.metricsInstrumentationKey = metricsInstrumentationKey;
    }

    public String getMetricsNamespace() {
        return metricsNamespace;
    }

    public void setMetricsNamespace(String metricsNamespace) {
        this.metricsNamespace = metricsNamespace;
    }

    public String getMetricsSuffix() {
        return metricsSuffix;
    }

    public void setMetricsSuffix(String metricsSuffix) {
        this.metricsSuffix = metricsSuffix;
    }

    public String getEventhubNamespace() {
        return eventhubNamespace;
    }

    public void setEventhubNamespace(String eventhubNamespace) {
        this.eventhubNamespace = eventhubNamespace;
    }

    public String getEventhubName() {
        return eventhubName;
    }

    public void setEventhubName(String eventhubName) {
        this.eventhubName = eventhubName;
    }

    public String getEventhubAccessKeyname() {
        return eventhubAccessKeyname;
    }

    public void setEventhubAccessKeyname(String eventhubAccessKeyname) {
        this.eventhubAccessKeyname = eventhubAccessKeyname;
    }

    public String getEventhubAccessKey() {
        return eventhubAccessKey;
    }

    public void setEventhubAccessKey(String eventhubAccessKey) {
        this.eventhubAccessKey = eventhubAccessKey;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getScaleSetId() {
        return scaleSetId;
    }

    public void setScaleSetId(String scaleSetId) {
        this.scaleSetId = scaleSetId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }
}
