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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.tomtom.cloud.recycling.MetricsPublisher;
import com.tomtom.cloud.recycling.Metric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.Arrays;


@RunWith(MockitoJUnitRunner.class)
public class AwsMetricsPublisherTest {

    @Mock
    private AmazonCloudWatchClient acwClient;

    @Mock
    private Metric metric;

    private MetricsPublisher metricsPublisher;

    @Before
    public void setUp() throws Exception {
        metricsPublisher = new AwsMetricsPublisher(Arrays.asList(metric),
                "cloud",
                10,
                acwClient);
        doReturn("TestMetric").when(metric).getName();
        doReturn(10d).when(metric).getValue();
    }

    @Test
    public void shouldPublishMetricsSuccessful() throws Exception {
        metricsPublisher.publish();
        verify(acwClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
    }

    @Test
    public void shouldHaveOneFailedPublishingRequests() throws Exception {
        doThrow(Exception.class).when(acwClient).putMetricData(any(PutMetricDataRequest.class));
        metricsPublisher.publish();
        assertEquals(1, metricsPublisher.getFailedPublishingRequests().size());
    }

}
