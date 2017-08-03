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
package com.tomtom.cloud.recycling.aws.cloudwatch;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.tomtom.cloud.recycling.OneTagMetricMap;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import com.tomtom.cloud.recycling.Metric;
import com.tomtom.cloud.recycling.MetricsPublisher;

/**
 * Class used for publishing metric data to AWS CloudWatch.
 * <p>
 * This class strictly belongs to 'tomtom' Spring profile and should be instantiated only if such profile was activated.
 */
@ManagedResource("Healer:component=worker,sub=cloudWatchMetricsPublisher")
public class AwsMetricsPublisher implements MetricsPublisher {

    /**
     * The metrics we want to have exposed to CloudWatch.
     */
    private final List<Metric> metrics;

    /**
     * The name space where metrics will be pushed.
     */
    private final String namespace;

    /**
     * The (connection/socket) timeout in seconds for the Amazon CloudWatch client.
     */
    private final int timeoutSeconds;

    /**
     * The client used for pushing metric data to CloudWatch.
     */
    private final AmazonCloudWatchClient client;

    /**
     * Book-keeping for the failed pushes.
     */
    private final OneTagMetricMap<String> failedPublishingRequests = new OneTagMetricMap<>("metricName");

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AwsMetricsPublisher.class);

    /**
     * Constructor that should be used during system initialization,
     * preferable in a configuration bean.
     *
     * @param metrics        list of metrics
     * @param namespace      the namespace where the metrics are published
     * @param timeoutSeconds timeout in seconds
     * @param client         aws client
     */
    public AwsMetricsPublisher(final List<Metric> metrics,
                               final String namespace,
                               final int timeoutSeconds,
                               final AmazonCloudWatchClient client) {
        this.metrics = metrics;
        this.timeoutSeconds = timeoutSeconds;
        this.namespace = namespace;
        this.client = client;
    }

    /**
     * Publishes metric data to AWS CloudWatch.
     */
    @Override
    public void publish() {
        putMetricData();
    }

    @ManagedMetric
    @Override
    public Map<Map<String, String>, ? extends Number> getFailedPublishingRequests() {
        return failedPublishingRequests.getCounts();
    }

    @ManagedAttribute
    @Override
    public String getNamespace() {
        return namespace;
    }

    @ManagedAttribute
    @Override
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    private void putMetricData() {
        for (final Metric metric : metrics) {
            final MetricDatum dataPoint = new MetricDatum()
                    .withMetricName(metric.getName()).withValue(metric.getValue());

            final PutMetricDataRequest request = new PutMetricDataRequest()
                    .withNamespace(namespace).withMetricData(dataPoint);

            try {
                client.putMetricData(request);
            } catch (Exception e) {
                failedPublishingRequests.increment(metric.getName());
                LOG.error("Could not push MetricData for {} to Amazon CloudWatch because of: {}", metric.getName(),
                        e.getMessage(), e);
            }
        }
    }

}
