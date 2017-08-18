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
package com.tomtom.cloud.recycling.azure.anh;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.tomtom.cloud.recycling.ShutdownNotifiсationPublisher;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventHubClient.class)
public class AzureShutdownNotifiсationPublisherTest {

    private static final String TEST_REASON = "reason";

    private EventHubClient client;

    private static final String TEST_INSTANCE_ID = "test_instance_id";

    private static final String TEST_TOPIC_NAME = "test_topic_name";

    @Before
    public void setUp() throws Exception {
        client = mock(EventHubClient.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldPublishShutdownAdvisedNotificationSuccessfully() throws Exception {
        ShutdownNotifiсationPublisher shutdownNotifiсationPublisher = new AzureShutdownNotifiсationPublisher(TEST_INSTANCE_ID, TEST_TOPIC_NAME, client);
        shutdownNotifiсationPublisher.publishShutdownNotification(TEST_REASON);
        verify(client, times(1)).send(any(EventData.class));
    }

    @Test
    public void shouldNotPublishShutdownAdvisedNotificationWhenInstanceIdIsNull() throws Exception {
        ShutdownNotifiсationPublisher shutdownNotifiсationPublisher = new AzureShutdownNotifiсationPublisher(null, TEST_TOPIC_NAME, client);
        shutdownNotifiсationPublisher.publishShutdownNotification(TEST_REASON);
        verifyZeroInteractions(client);
    }

    @Test
    public void shouldNotPublishShutdownAdvisedNotificationWhenTopicNameIsNull() throws Exception {
        ShutdownNotifiсationPublisher shutdownNotifiсationPublisher = new AzureShutdownNotifiсationPublisher(TEST_INSTANCE_ID, null, client);
        shutdownNotifiсationPublisher.publishShutdownNotification(TEST_REASON);
        verifyZeroInteractions(client);
    }

    @Test
    public void shouldNotPublishShutdownAdvisedNotificationWhenClientIsNull() throws Exception {
        ShutdownNotifiсationPublisher shutdownNotifiсationPublisher = new AzureShutdownNotifiсationPublisher(TEST_INSTANCE_ID, TEST_TOPIC_NAME, null);
        shutdownNotifiсationPublisher.publishShutdownNotification(TEST_REASON);
    }

}