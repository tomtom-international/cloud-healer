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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyZeroInteractions;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MetricsUpdaterImplTest {

    @Mock
    private MetricsPublisher metricsPublisher;

    private MetricsUpdater metricsUpdater;

    @Before
    public void setUp() throws Exception {
        metricsUpdater = new MetricsUpdaterImpl(metricsPublisher, 1, true);
    }

    @Test
    public void isRunningShouldBeTrue() throws Exception {
        metricsUpdater.start();
        assertTrue(metricsUpdater.isRunning());
        metricsUpdater.stop();
    }

    @Test
    public void shouldPublishSuccessful() throws Exception {
        metricsUpdater.start();
        Thread.sleep(1000);
        verify(metricsPublisher, atLeast(1)).publish();
        metricsUpdater.stop();
    }

    @Test
    public void shouldNotPublish() throws Exception {
        metricsUpdater.setEnabled(false);
        metricsUpdater.start();
        Thread.sleep(1000);
        verifyZeroInteractions(metricsPublisher);
        metricsUpdater.stop();
    }

    @Test
    public void shouldFailPublishing() throws Exception {
        doThrow(Exception.class).when(metricsPublisher).publish();
        metricsUpdater.start();
        Thread.sleep(1000);
        verify(metricsPublisher, atLeast(1)).publish();
        metricsUpdater.stop();
    }

    @Test
    public void isRunningShouldBeFalse() throws Exception {
        assertFalse(metricsUpdater.isRunning());
    }

    @Test
    public void threadShouldNotBeLounchTwice() throws Exception {
        metricsUpdater.start();
        metricsUpdater.start();
    }

    @Test
    public void stopShouldDoNothing() throws Exception {
        metricsUpdater.stop();
    }

}
