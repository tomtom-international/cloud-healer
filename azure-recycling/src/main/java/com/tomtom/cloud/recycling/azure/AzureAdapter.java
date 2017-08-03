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
package com.tomtom.cloud.recycling.azure;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVM;
import com.tomtom.cloud.recycling.CloudAdapter;
import com.tomtom.cloud.recycling.MonitoringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import com.tomtom.cloud.recycling.azure.client.AzureRestClient;

/**
 * Adapter meant to be used for all direct interactions with Azure.
 * This class is just a skeleton and should be implemented when
 * a new release of azure-sdk-for-java will enable us to do proper
 * health checks for application gateway.
 */
@ManagedResource("Healer:component=worker,sub=azureAdapter")
public class AzureAdapter implements CloudAdapter {

    private final String instanceId;

    private final int timeoutSeconds;

    private final VirtualMachineScaleSet scaleSet;

    private final AzureRestClient azureRestClient;

    private static final Logger LOG = LoggerFactory.getLogger(AzureAdapter.class);

    public AzureAdapter(final String instanceId,
                        final int timeoutSeconds,
                        final VirtualMachineScaleSet scaleSet,
                        final AzureRestClient azureRestClient) {
        this.instanceId = instanceId;
        this.timeoutSeconds = timeoutSeconds;
        this.scaleSet = scaleSet;
        this.azureRestClient = azureRestClient;
    }


    @Override
    public void doubleAutoScalingGroupSize() throws MonitoringException {
        scaleSet.update().withCapacity((int) scaleSet.capacity() * 2).apply();
    }

    @Override
    public void terminateCurrentInstance() {
        Optional<VirtualMachineScaleSetVM> currentInstance = scaleSet
                .virtualMachines()
                .list()
                .stream()
                .filter(vm -> vm.instanceId().equalsIgnoreCase(instanceId))
                .findFirst();
        if (currentInstance.isPresent()) {
            currentInstance.get().deallocate();
        }
    }

    @Override
    public boolean areAllInstancesHealthy() throws MonitoringException {
        AzureRestClient restClient = azureRestClient.authenticate();
        URI uri = restClient.readHealthCheckLocation();
        try {
            return restClient.areAllInstancesHealthy(uri);
        } catch (ExecutionException | InterruptedException e) {
            throw new MonitoringException(e);
        }
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
