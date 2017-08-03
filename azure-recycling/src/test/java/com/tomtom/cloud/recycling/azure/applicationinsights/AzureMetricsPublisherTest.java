/*************************************************************************************************
 * Copyright (c) 1992-2017 TomTom N.V. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom N.V. and its subsidiaries and may be used
 * for internal evaluation purposes or commercial use strictly subject to separate licensee
 * agreement between you and TomTom. If you are the licensee, you are only permitted to use
 * this Software in accordance with the terms of your license agreement. If you are not the
 * licensee then you are not authorised to use this software in any manner and should
 * immediately return it to TomTom N.V.
 ************************************************************************************************/
package com.tomtom.cloud.recycling.azure.applicationinsights;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.microsoft.applicationinsights.TelemetryClient;
import com.tomtom.cloud.recycling.Metric;
import com.tomtom.cloud.recycling.MetricsPublisher;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;


public class AzureMetricsPublisherTest {

    private MetricsPublisher metricsPublisher;

    private TelemetryClient telemetryClient;

    private List<Metric> metrics;

    private Metric metricFlow;

    private Metric metricTraffic;

    @Before
    public void setUp() throws Exception {
        telemetryClient = mock(TelemetryClient.class);
        metricFlow = mock(Metric.class);
        metricTraffic = mock(Metric.class);
        doReturn("flow").when(metricFlow).getName();
        doReturn(1d).when(metricFlow).getValue();
        doReturn("traffic").when(metricTraffic).getName();
        doReturn(1d).when(metricTraffic).getValue();

        metrics = Arrays.asList(metricFlow, metricTraffic);
        metricsPublisher = new AzureMetricsPublisher(
                metrics, "cloud",
                "cloud", telemetryClient
        );
    }

    @Test
    public void shouldPublishSuccessfully() throws Exception {
        metricsPublisher.publish();
        assertEquals(0, metricsPublisher.getFailedPublishingRequests().size());
    }

    @Test
    public void shouldHaveOnePublishingError() throws Exception {
        doThrow(Exception.class).when(metricFlow).getValue();
        metricsPublisher.publish();
        assertEquals(1, metricsPublisher.getFailedPublishingRequests().size());
    }

    @Test
    public void shouldHaveMetricNamespace() {
        assertNotNull(metricsPublisher.getNamespace());
    }

    @Test
    public void shouldHaveTimeoutEqualToZero() {
        assertEquals(0, metricsPublisher.getTimeoutSeconds());
    }

}
