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
package com.tomtom.cloud.recycling.aws;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

import java.util.ArrayList;
import java.util.Arrays;

import com.tomtom.cloud.recycling.MonitoringException;

@RunWith(MockitoJUnitRunner.class)
public class ElbAdapterTest {

    @Mock
    private AmazonElasticLoadBalancingClient mockELBClient;

    @Mock
    private DescribeLoadBalancersResult loadBalancersResult;

    private ElbAdapter testedAdapter;

    @Test
    public void testAreAllInstancesHealthy() throws Exception {
        // load balancer name
        Instance elbInstance = new Instance().withInstanceId("testInstanceId");
        LoadBalancerDescription elbDescription = new LoadBalancerDescription().withInstances(elbInstance).withLoadBalancerName("test");
        DescribeLoadBalancersResult elbResult = new DescribeLoadBalancersResult().withLoadBalancerDescriptions(elbDescription);
        when(mockELBClient.describeLoadBalancers()).thenReturn(elbResult);

        // load balancer instances health
        InstanceState elbInstanceState = new InstanceState().withState("InService");
        DescribeInstanceHealthResult healthResult = new DescribeInstanceHealthResult().withInstanceStates(elbInstanceState);
        when(mockELBClient.describeInstanceHealth(any(DescribeInstanceHealthRequest.class))).thenReturn(healthResult);

        testedAdapter = new ElbAdapter(mockELBClient, "testInstanceId");

        assertTrue(testedAdapter.areAllInstancesHealthy());

        verify(mockELBClient).describeLoadBalancers();
        verify(mockELBClient).describeInstanceHealth(any(DescribeInstanceHealthRequest.class));
    }


    @Test
    public void testUnHealthyInstances() throws Exception {
        // load balancer name
        Instance elbInstance = new Instance().withInstanceId("testInstanceId");
        LoadBalancerDescription elbDescription = new LoadBalancerDescription().withInstances(elbInstance).withLoadBalancerName("test");
        DescribeLoadBalancersResult elbResult = new DescribeLoadBalancersResult().withLoadBalancerDescriptions(elbDescription);
        when(mockELBClient.describeLoadBalancers()).thenReturn(elbResult);

        // load balancer instances health
        InstanceState elbInstanceState = new InstanceState().withState("OutOfService");
        DescribeInstanceHealthResult healthResult = new DescribeInstanceHealthResult().withInstanceStates(elbInstanceState);
        when(mockELBClient.describeInstanceHealth(any(DescribeInstanceHealthRequest.class))).thenReturn(healthResult);

        testedAdapter = new ElbAdapter(mockELBClient, "testInstanceId");

        assertFalse(testedAdapter.areAllInstancesHealthy());

        verify(mockELBClient).describeLoadBalancers();
        verify(mockELBClient).describeInstanceHealth(any(DescribeInstanceHealthRequest.class));

    }

    @Test
    public void testGetLoadBalancerNameOnlyOnce() throws Exception {
        // load balancer name
        Instance elbInstance = new Instance().withInstanceId("testInstanceId");
        Instance elbInstance2 = new Instance().withInstanceId("testInstanceId2");
        LoadBalancerDescription elbDescription = new LoadBalancerDescription().withInstances(elbInstance2, elbInstance).withLoadBalancerName("test");
        DescribeLoadBalancersResult elbResult = new DescribeLoadBalancersResult().withLoadBalancerDescriptions(elbDescription);
        when(mockELBClient.describeLoadBalancers()).thenReturn(elbResult);

        // load balancer instances health
        InstanceState elbInstanceState = new InstanceState().withState("InService");
        DescribeInstanceHealthResult healthResult = new DescribeInstanceHealthResult().withInstanceStates(elbInstanceState);
        when(mockELBClient.describeInstanceHealth(any(DescribeInstanceHealthRequest.class))).thenReturn(healthResult);

        testedAdapter = new ElbAdapter(mockELBClient, "testInstanceId");

        assertTrue(testedAdapter.areAllInstancesHealthy());
        assertTrue(testedAdapter.areAllInstancesHealthy());

        verify(mockELBClient, times(1)).describeLoadBalancers();
        verify(mockELBClient, times(2)).describeInstanceHealth(any(DescribeInstanceHealthRequest.class));
    }

    @Test(expected = MonitoringException.class)
    public void shouldThrowExceptionWhenNoLbAreReturned() throws MonitoringException {
        doReturn(loadBalancersResult).when(mockELBClient).describeLoadBalancers();
        doReturn(new ArrayList<>()).when(loadBalancersResult).getLoadBalancerDescriptions();
        testedAdapter = new ElbAdapter(mockELBClient, "testInstanceId");
        testedAdapter.areAllInstancesHealthy();
    }

    @Test(expected = MonitoringException.class)
    public void shouldThrowExceptionWhenInstancesAreReturned() throws MonitoringException {
        LoadBalancerDescription loadBalancerDescription = mock(LoadBalancerDescription.class);
        doReturn(loadBalancersResult).when(mockELBClient).describeLoadBalancers();
        doReturn(Arrays.asList(loadBalancerDescription)).when(loadBalancersResult).getLoadBalancerDescriptions();
        doReturn(new ArrayList<>()).when(loadBalancerDescription).getInstances();
        testedAdapter = new ElbAdapter(mockELBClient, "testInstanceId");
        testedAdapter.areAllInstancesHealthy();
    }
}
