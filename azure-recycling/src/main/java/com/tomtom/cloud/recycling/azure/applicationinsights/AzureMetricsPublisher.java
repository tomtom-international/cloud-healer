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
package com.tomtom.cloud.recycling.azure.applicationinsights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.applicationinsights.TelemetryClient;
import com.tomtom.cloud.recycling.OneTagMetricMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import com.tomtom.cloud.recycling.Metric;
import com.tomtom.cloud.recycling.MetricsPublisher;

/**
 * Class used for publishing metric data to Azure Application Insights.
 * <p>
 * This class strictly belongs to 'azure' Spring profile and should be
 * instantiated only if such profile was activated.
 */
@ManagedResource("Healer:component=worker,sub=applicationInsightsMetricsPublisher")
public class AzureMetricsPublisher implements MetricsPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AzureMetricsPublisher.class);
    private final String suffix;
    private final String namespace;
    private final TelemetryClient client;
    private final List<Metric> metrics;
    private final Map<String, String> properties;
    /**
     * Book-keeping for the failed pushes.
     */
    private final OneTagMetricMap<String> failedPublishingRequests = new OneTagMetricMap<>("metricName");

    /**
     * Constructor that should be used during system initialization, preferably in a configuration bean.
     *
     * @param metrics   metrics we want to expose in Application Insights
     * @param suffix    metric name suffix
     * @param namespace metric namespace
     * @param client    Azure telemetry client
     */
    public AzureMetricsPublisher(final List<Metric> metrics, final String suffix,
                                 final String namespace, final TelemetryClient client) {
        this.suffix = suffix;
        this.namespace = namespace;
        this.client = client;
        this.metrics = metrics;
        properties = new HashMap<>();
        properties.put("namespace", this.namespace);
    }

    @Override
    public void publish() {
        for (Metric metric : metrics) {
            final String name = metric.getName() + suffix;
            try {
                client.trackMetric(name, metric.getValue(),
                        1, metric.getValue(), metric.getValue(),
                        properties);
            } catch (Exception e) {
                failedPublishingRequests.increment(name);
                LOG.error("Could not push MetricData for {} to Azure Application Insights because of: {}",
                        name, e.getMessage(), e);
            }
        }
    }

    @ManagedMetric
    @Override
    public Map<Map<String, String>, ? extends Number> getFailedPublishingRequests() {
        return failedPublishingRequests.getCounts();
    }

    @ManagedAttribute
    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @ManagedAttribute
    @Override
    public int getTimeoutSeconds() {
        return 0;
    }
}
