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

import com.amazonaws.services.autoscaling.model.ExecutePolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DeletePolicyRequest;
import com.amazonaws.services.autoscaling.model.ExecutePolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupRequest;
import com.tomtom.cloud.recycling.MonitoringException;

/**
 * Adapter that handles AWS AutoScaling operations.
 */
public class AutoScalingAdapter {

    private final AmazonAutoScalingClient asClient;

    private final String instanceId;

    private static final Logger LOG = LoggerFactory.getLogger(AutoScalingAdapter.class);

    public AutoScalingAdapter(final AmazonAutoScalingClient asClient, final String instanceId) {
        this.asClient = asClient;
        this.instanceId = instanceId;
    }

    void doubleGroupSize() throws MonitoringException {
        final String scalingPolicyName = "DoubleScalingGroupSize-" + instanceId;
        final int scalingDoubleGroupSizeAdjustment = 100;

        try {
            final String scalingGroupName = getScalingGroupName();
            LOG.info("Creating scaling policy {}.", scalingPolicyName);
            final PutScalingPolicyResult scalingPolicyResult = asClient.putScalingPolicy(new PutScalingPolicyRequest().
                        withAutoScalingGroupName(scalingGroupName).
                        withPolicyName(scalingPolicyName).
                        withAdjustmentType("PercentChangeInCapacity").
                        withScalingAdjustment(scalingDoubleGroupSizeAdjustment).
                        withCooldown(300));
            final String scalingPolicyARN = scalingPolicyResult.getPolicyARN();

            LOG.info("Doubling the number of instances in the {} scaling group.", scalingGroupName);
            ExecutePolicyResult result = asClient.executePolicy(new ExecutePolicyRequest().withPolicyName(scalingPolicyARN));
            if(result!= null && result.getSdkHttpMetadata() != null) {
                LOG.info("Doubling finished with Http code {}", result.getSdkHttpMetadata().getHttpStatusCode());
            } else {
                LOG.info("Doubling finished with null SdkHttp metadata");
            }
//            LOG.info("Removing scaling policy {}.", scalingPolicyName);
//            asClient.deletePolicy(new DeletePolicyRequest().withPolicyName(scalingPolicyARN));
        } catch (Exception cause) {
            final String message = "Error doubling the number of scaling group instances: " + cause.getMessage();
            throw new MonitoringException(message, cause);
        }
    }

    void terminateCurrentInstance() throws MonitoringException {
        try {
            LOG.info("Terminate instance {}.", instanceId);
            final TerminateInstanceInAutoScalingGroupRequest terminateRequest =
                    new TerminateInstanceInAutoScalingGroupRequest().
                        withInstanceId(instanceId).
                        withShouldDecrementDesiredCapacity(true);

            final Object terminateResult = asClient.terminateInstanceInAutoScalingGroup(terminateRequest);

            LOG.info("Terminate instance request sent: {}.", terminateResult);
        } catch (Exception cause) {
            final String message = "Error while terminating instance from auto scaling group: " + cause.getMessage();
            throw new MonitoringException(message, cause);
        }
    }

    private String getScalingGroupName() throws MonitoringException {
        for (AutoScalingGroup autoScalingGroup : asClient.describeAutoScalingGroups().getAutoScalingGroups()) {
            for (com.amazonaws.services.autoscaling.model.Instance instance : autoScalingGroup.getInstances()) {
                if (instanceId.equals(instance.getInstanceId())) {
                    return autoScalingGroup.getAutoScalingGroupName();
                }
            }
        }

        throw new MonitoringException("No scaling group found for instance " + instanceId);
    }
}
