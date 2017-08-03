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

import java.io.IOException;
import java.util.List;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.tomtom.cloud.recycling.CloudAdapter;
import com.tomtom.cloud.recycling.Metric;
import com.tomtom.cloud.recycling.MetricsPublisher;
import com.tomtom.cloud.recycling.azure.AzureAdapter;
import com.tomtom.cloud.recycling.azure.applicationinsights.AzureMetricsPublisher;
import com.tomtom.cloud.recycling.azure.client.AzureRestClient;
import com.tomtom.cloud.recycling.azure.client.AzureRestConfig;

/**
 * Configuration class for Azure monitoring.
 */
@ConditionalOnProperty(name = "cloud.azure.enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties({AzureProperties.class, AzureCredentials.class})
public class AzureMonitoringAutoConfig {

    @Bean
    public TelemetryClient telemetryClient(final AzureProperties properties) {
        final TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
        if (properties.getMetricsInstrumentationKey() != null) {
            configuration.setInstrumentationKey(properties.getMetricsInstrumentationKey());
        }
        return new TelemetryClient(configuration);
    }

    @Bean
    public MetricsPublisher metricsPublisher(final AzureProperties properties,
                                             final List<Metric> metrics,
                                             final TelemetryClient telemetryClient) {
        return new AzureMetricsPublisher(metrics,
                properties.getMetricsSuffix(),
                properties.getMetricsNamespace(),
                telemetryClient);
    }

    @Bean
    public EventHubClient eventHubClient(final AzureProperties properties) throws IOException, ServiceBusException {
        ConnectionStringBuilder connection = new ConnectionStringBuilder(
                properties.getEventhubNamespace(),
                properties.getEventhubName(),
                properties.getEventhubAccessKeyname(),
                properties.getEventhubAccessKey());
        return EventHubClient.createFromConnectionStringSync(connection.toString());
    }

    @Bean
    public ApplicationTokenCredentials tokenCredentials(final AzureCredentials credentials) {
        return new ApplicationTokenCredentials(
                credentials.getClientId(),
                credentials.getTenant(),
                credentials.getClientSecret(),
                AzureEnvironment.AZURE);
    }

    @Bean
    public AzureRestClient restClient(final AzureProperties properties,
                                      final ApplicationTokenCredentials tokenCredentials) {
        AzureRestConfig config = AzureRestConfig.builder()
                .health(properties.getHealth())
                .resource(tokenCredentials.environment().managementEndpoint())
                .clientId(tokenCredentials.getClientId())
                .clientSecret(tokenCredentials.getSecret())
                .resourceGroupName(properties.getGroupName())
                .appGatewayName(properties.getGatewayName())
                .subscriptionId(properties.getSubscription())
                .apiVersion(properties.getApiVersion())
                .authority(tokenCredentials.environment().authenticationEndpoint() +
                        tokenCredentials.domain() + "/oauth2/token")
                .build();
        return new AzureRestClient(config);
    }

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    public Azure azure(final AzureTokenCredentials tokenCredentials) throws IOException {
        return Azure.authenticate(tokenCredentials).withDefaultSubscription();
    }

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    public CloudAdapter cloudAdapter(final AzureProperties properties,
                                     final Azure azure,
                                     final AzureRestClient restClient) throws IOException {
        VirtualMachineScaleSet virtualMachineScaleSet = azure.virtualMachineScaleSets()
                .getById(properties.getScaleSetId());
        return new AzureAdapter(properties.getInstance(), properties.getTimeout(), virtualMachineScaleSet, restClient);
    }

}
