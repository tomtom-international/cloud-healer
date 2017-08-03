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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.stream.Stream;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet.UpdateStages.WithPrimaryLoadBalancer;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVM;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMs;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet.UpdateStages.WithApply;
import com.tomtom.cloud.recycling.azure.AzureAdapter;
import org.junit.Before;
import org.junit.Test;
import com.tomtom.cloud.recycling.azure.client.AzureRestClient;


public class AzureAdapterTest {

    private static final int TEST_TIMEOUT = 42;

    private static final String TEST_INSTANCE_ID = "testInstanceId";

    private AzureAdapter azureAdapter;

    private VirtualMachineScaleSet vmSet;

    private AzureRestClient azureRestClient;

    @Before
    public void setUp() throws Exception {
        vmSet = mock(VirtualMachineScaleSet.class);
        azureRestClient = mock(AzureRestClient.class);
        azureAdapter = new AzureAdapter(TEST_INSTANCE_ID, TEST_TIMEOUT, vmSet, azureRestClient);
    }

    @Test
    public void shouldDoubleAutoScalingGroupSize() throws Exception {
        doReturn(2l).when(vmSet).capacity();
        WithPrimaryLoadBalancer primaryLoadBalancer = mock(WithPrimaryLoadBalancer.class);
        doReturn(primaryLoadBalancer).when(vmSet).update();
        WithApply withApply = mock(WithApply.class);
        doReturn(withApply).when(primaryLoadBalancer).withCapacity(4);
        azureAdapter.doubleAutoScalingGroupSize();
        verify(primaryLoadBalancer, times(1)).withCapacity(4);
    }

    @Test
    public void shouldTerminateCurrentInstance() throws Exception {
        VirtualMachineScaleSetVM scaleSetVM = createMockForTerminateInstance(TEST_INSTANCE_ID);

        azureAdapter.terminateCurrentInstance();
        verify(scaleSetVM, times(1)).deallocate();
    }


    @Test
    public void shouldNotTerminateCurrentInstance() throws Exception {
        VirtualMachineScaleSetVM scaleSetVM = createMockForTerminateInstance("differentInstanceId");

        azureAdapter.terminateCurrentInstance();
        verify(scaleSetVM, times(0)).deallocate();
    }

    @Test
    public void areAllInstancesHealthyShouldBeTrue() throws Exception {
        doReturn(azureRestClient).when(azureRestClient).authenticate();
        doReturn(true).when(azureRestClient).areAllInstancesHealthy(any(URI.class));
        assertTrue(azureAdapter.areAllInstancesHealthy());
    }

    @Test
    public void areAllInstancesHealthyShouldBeFalse() throws Exception {
        doReturn(azureRestClient).when(azureRestClient).authenticate();
        doReturn(false).when(azureRestClient).areAllInstancesHealthy(any(URI.class));
        assertFalse(azureAdapter.areAllInstancesHealthy());
    }

    @Test
    public void shouldHaveAnInstanceIdEqualWithTestInstanceId() throws Exception {
        assertEquals(TEST_INSTANCE_ID, azureAdapter.getInstanceId());
    }

    @Test
    public void shouldHaveTimeoutEqualTo42() throws Exception {
        assertEquals(TEST_TIMEOUT, azureAdapter.getTimeoutSeconds());
    }

    private VirtualMachineScaleSetVM createMockForTerminateInstance(String instanceId) {
        VirtualMachineScaleSetVM scaleSetVM = mock(VirtualMachineScaleSetVM.class);
        doReturn(instanceId).when(scaleSetVM).instanceId();
        PagedList<VirtualMachineScaleSetVM> vms = mock(PagedList.class);
        Stream<VirtualMachineScaleSetVM> stream = Stream.of(scaleSetVM);
        doReturn(stream).when(vms).stream();
        VirtualMachineScaleSetVMs mockVmScaleSetVMs = mock(VirtualMachineScaleSetVMs.class);
        doReturn(vms).when(mockVmScaleSetVMs).list();
        doReturn(mockVmScaleSetVMs).when(vmSet).virtualMachines();
        return scaleSetVM;
    }


}