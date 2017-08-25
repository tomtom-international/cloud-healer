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

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tomtom.cloud.recycling.MonitoringException;

/**
 * Adapter that handles AWS ELB operations.
 */
public class ElbAdapter {

    private final AmazonElasticLoadBalancingClient elbClient;
    private final String instanceId;

    private String loadBalancerName = null;

    private static final Logger LOG = LoggerFactory.getLogger(ElbAdapter.class);

    public ElbAdapter(final AmazonElasticLoadBalancingClient elbClient, final String instanceId) {
        this.elbClient = elbClient;
        this.instanceId = instanceId;
    }

    boolean areAllInstancesHealthy() throws MonitoringException {
        if (loadBalancerName == null) {
            loadBalancerName = getLoadBalancerName();
        }

        final DescribeInstanceHealthRequest healthRequest = new DescribeInstanceHealthRequest()
            .withLoadBalancerName(loadBalancerName);

        int totalInstances = 0, unhealthyInstances = 0;
        for (InstanceState state : elbClient.describeInstanceHealth(healthRequest).getInstanceStates()) {
            totalInstances++;
            if (ElbInstanceState.IN_SERVICE != ElbInstanceState.fromString(state.getState())) {
                unhealthyInstances++;
            }
        }
        LOG.info("Checked load balancer {} instances health: {} unhealthy instances out of {} total.",
            loadBalancerName, unhealthyInstances, totalInstances);

        return unhealthyInstances == 0;
    }

    private String getLoadBalancerName() throws MonitoringException {
        for (LoadBalancerDescription loadBalancer : elbClient.describeLoadBalancers().getLoadBalancerDescriptions()) {
            for (Instance instance : loadBalancer.getInstances()) {
                if (instanceId.equals(instance.getInstanceId())) {
                    return loadBalancer.getLoadBalancerName();
                }
            }
        }

        throw new MonitoringException("No elastic load balancer found for instance " + instanceId);
    }
}
