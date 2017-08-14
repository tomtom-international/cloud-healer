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
package com.tomtom.cloud.recycling.aws.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tomtom.cloud.recycling.CloudAdapter;
import com.tomtom.cloud.recycling.Metric;
import com.tomtom.cloud.recycling.MetricsPublisher;
import com.tomtom.cloud.recycling.ShutdownAdvisedNotifier;
import com.tomtom.cloud.recycling.aws.AwsAdapter;
import com.tomtom.cloud.recycling.aws.AwsConstants;
import com.tomtom.cloud.recycling.aws.cloudwatch.AwsMetricsPublisher;
import com.tomtom.cloud.recycling.aws.sns.AwsShutdownAdvisedNotifier;


/**
 * Configuration class for AWS recycling.
 */
@ConditionalOnProperty(name="graceful.recycling.aws.enabled", havingValue="true")
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsRecyclingAutoConfig {

    @Bean
    public AmazonCloudWatchClient acwClient(final AwsProperties properties) {
        final ClientConfiguration cc = new ClientConfiguration();
        cc.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(properties.getTimeout()));
        cc.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(properties.getTimeout()));

        AmazonCloudWatchClient client = new AmazonCloudWatchClient(cc);
        client.setRegion(AwsConstants.AWS_REGION);
        return client;
    }

    @Bean
    public MetricsPublisher metricsPublisher(
            final AwsProperties properties,
            final List<Metric> metrics,
            final AmazonCloudWatchClient acwClient) {
        return new AwsMetricsPublisher(metrics,
                properties.getNamespace(),
                properties.getTimeout(),
                acwClient);
    }

    @Bean
    public AmazonSNSClient snsClient(final AwsProperties properties) {
        final ClientConfiguration cc = new ClientConfiguration();
        cc.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(properties.getTimeout()));
        cc.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(properties.getTimeout()));

        AmazonSNSClient client = new AmazonSNSClient(cc);
        client.setRegion(AwsConstants.AWS_REGION);
        return client;
    }

    @Bean
    public ShutdownAdvisedNotifier shutdownAdvisedNotifier(final AwsProperties properties,
                                                           final AmazonSNSClient snsClient) {
        return new AwsShutdownAdvisedNotifier(properties.getInstance(), properties.getTopic(), snsClient);
    }

    @Bean
    public CloudAdapter cloudAdapter(final AwsProperties properties) {
        return new AwsAdapter(properties.getInstance(), properties.getTimeout());
    }
}
