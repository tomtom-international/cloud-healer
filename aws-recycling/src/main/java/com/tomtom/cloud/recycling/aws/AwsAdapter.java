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

import java.util.concurrent.TimeUnit;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.tomtom.cloud.recycling.CloudAdapter;
import com.tomtom.cloud.recycling.MonitoringException;
import com.tomtom.cloud.recycling.WorkerRecycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Amazon adapter meant to be used for all direct interactions with AWS.
 */
@ManagedResource("Healer:component=worker,sub=awsAdapter")
public class AwsAdapter implements CloudAdapter {

    private final String instanceId;
    private final int timeoutSeconds;
    private AutoScalingAdapter autoScalingAdapter;
    private ElbAdapter elbAdapter;

    private static final Logger LOG = LoggerFactory.getLogger(AwsAdapter.class);

    public AwsAdapter(
            final String instanceId,
            final int timeoutSeconds) {
        this.instanceId = instanceId;
        LOG.info("initializing with instanceId " + instanceId);
        this.timeoutSeconds = timeoutSeconds;

        final ClientConfiguration cc = new ClientConfiguration();
        cc.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));
        cc.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));

        final AmazonAutoScalingClient asClient = new AmazonAutoScalingClient(cc);
        asClient.setRegion(AwsConstants.AWS_REGION);
        autoScalingAdapter = new AutoScalingAdapter(asClient, instanceId);

        final AmazonElasticLoadBalancingClient elbClient = new AmazonElasticLoadBalancingClient(cc);
        elbClient.setRegion(AwsConstants.AWS_REGION);
        elbAdapter = new ElbAdapter(elbClient, instanceId);

    }

    @Override
    public void doubleAutoScalingGroupSize() throws MonitoringException {
        autoScalingAdapter.doubleGroupSize();
    }

    @Override
    public void terminateCurrentInstance() throws MonitoringException {
        autoScalingAdapter.terminateCurrentInstance();
    }

    @Override
    public boolean areAllInstancesHealthy() throws MonitoringException {
        return elbAdapter.areAllInstancesHealthy();
    }

    @ManagedAttribute
    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @ManagedAttribute
    @Override
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
