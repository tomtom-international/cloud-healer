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
package com.tomtom.cloud.recycling;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomtom.cloud.recycling.CloudAdapter;
import com.tomtom.cloud.recycling.MonitoringException;
import com.tomtom.cloud.recycling.ThreadContext;
import com.tomtom.cloud.recycling.WorkerRecyclerThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WorkerRecyclerThreadTest {

    @Mock
    private CloudAdapter mockCloudAdapter;

    private WorkerRecyclerThread recycleThread;

    @Before
    public void setUp() throws Exception {
        when(mockCloudAdapter.areAllInstancesHealthy()).thenReturn(true);
        recycleThread = new WorkerRecyclerThread(mockCloudAdapter);

        // when the updater thread sleeps after completing the first update cycle, terminate it
        ThreadContext threadContext = new ThreadContext() {
            @Override
            public void sleep(final long millis) {
                recycleThread.interrupt();
            }
        };
        recycleThread.setThreadContext(threadContext);
    }

    @Test
    public void shouldRunSuccessfully() throws Exception {
        recycleThread.run();

        verify(mockCloudAdapter).doubleAutoScalingGroupSize();
        verify(mockCloudAdapter).areAllInstancesHealthy();
        verify(mockCloudAdapter).terminateCurrentInstance();
    }

    @Test
    public void shouldHandleExceptionsSuccessfully() throws Exception {
        doThrow(new MonitoringException("test")).when(mockCloudAdapter).doubleAutoScalingGroupSize();

        recycleThread.run();
    }

    @Test
    public void shouldHandleExceptionsWhileWaitingSuccessfully() throws Exception {
        doThrow(new RuntimeException("test")).when(mockCloudAdapter).areAllInstancesHealthy();
        recycleThread.run();
    }

    @Test
    public void shouldCheckForHealthyInstance60Times() throws Exception {
        doReturn(false).when(mockCloudAdapter).areAllInstancesHealthy();
        recycleThread.run();
    }
}
