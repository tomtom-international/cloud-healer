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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DeletePolicyRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.ExecutePolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import com.tomtom.cloud.recycling.MonitoringException;

@RunWith(MockitoJUnitRunner.class)
public class AutoScalingAdapterTest {

    @Mock
    private AmazonAutoScalingClient mockASClient;

    @Mock
    private DescribeAutoScalingGroupsResult autoScalingGroupsResult;

    private AutoScalingAdapter testedAdapter;

    @Test
    public void testDoubleGroupSize() throws Exception {
        // auto scaling group name
        com.amazonaws.services.autoscaling.model.Instance asInstance = new com.amazonaws.services.autoscaling.model.Instance().withInstanceId("testInstanceId");
        AutoScalingGroup asGroup = new AutoScalingGroup().withInstances(asInstance).withAutoScalingGroupName("testGroupName");
        DescribeAutoScalingGroupsResult asResult = new DescribeAutoScalingGroupsResult().withAutoScalingGroups(asGroup);
        when(mockASClient.describeAutoScalingGroups()).thenReturn(asResult);

        // scaling policy
        PutScalingPolicyResult scalingPolicyResult = new PutScalingPolicyResult().withPolicyARN("policyARN");
        when(mockASClient.putScalingPolicy(any(PutScalingPolicyRequest.class))).thenReturn(scalingPolicyResult);

        testedAdapter = new AutoScalingAdapter(mockASClient, "testInstanceId");

        testedAdapter.doubleGroupSize();

        verify(mockASClient).executePolicy(any(ExecutePolicyRequest.class));
        verify(mockASClient).deletePolicy(any(DeletePolicyRequest.class));
    }

    @Test(expected = MonitoringException.class)
    public void testPropagateExceptionForDoubleGroupSize() throws Exception {
        // exception while describing groups
        when(mockASClient.describeAutoScalingGroups()).thenThrow(new RuntimeException("test"));

        testedAdapter = new AutoScalingAdapter(mockASClient, "testInstanceId");

        testedAdapter.doubleGroupSize();
    }

    @Test
    public void testTerminateCurrentInstance() throws Exception {
        testedAdapter = new AutoScalingAdapter(mockASClient, "testInstanceId");

        testedAdapter.terminateCurrentInstance();

        verify(mockASClient).terminateInstanceInAutoScalingGroup(any(TerminateInstanceInAutoScalingGroupRequest.class));
    }

    @Test(expected = MonitoringException.class)
    public void testPropagateExceptionDoubleGroupSize() throws Exception {
        // exception while describing groups
        when(mockASClient.terminateInstanceInAutoScalingGroup(any(TerminateInstanceInAutoScalingGroupRequest.class))).thenThrow(new RuntimeException("test"));

        testedAdapter = new AutoScalingAdapter(mockASClient, "testInstanceId");

        testedAdapter.terminateCurrentInstance();
    }

    @Test(expected = MonitoringException.class)
    public void shouldThrowAWSExceptionWhenNoAutoScalingGroupsAreReturned() throws Exception {
        doReturn(new ArrayList<>()).when(autoScalingGroupsResult).getAutoScalingGroups();
        doReturn(autoScalingGroupsResult).when(mockASClient).describeAutoScalingGroups();
        testedAdapter = new AutoScalingAdapter(mockASClient, "testInstanceId");
        testedAdapter.doubleGroupSize();
    }

    @Test(expected = MonitoringException.class)
    public void shouldThrowAWSExceptionWhenNoInstancesAreReturned() throws Exception {
        AutoScalingGroup autoScalingGroup = mock(AutoScalingGroup.class);
        doReturn(new ArrayList<>()).when(autoScalingGroup).getInstances();
        doReturn(Arrays.asList(autoScalingGroup)).when(autoScalingGroupsResult).getAutoScalingGroups();
        doReturn(autoScalingGroupsResult).when(mockASClient).describeAutoScalingGroups();
        testedAdapter = new AutoScalingAdapter(mockASClient, "testInstanceId");
        testedAdapter.doubleGroupSize();
    }

}
