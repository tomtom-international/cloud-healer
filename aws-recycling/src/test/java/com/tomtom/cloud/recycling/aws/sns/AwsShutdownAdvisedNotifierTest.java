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
package com.tomtom.cloud.recycling.aws.sns;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.tomtom.cloud.recycling.ShutdownAdvisedNotifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static  org.hamcrest.Matchers.*;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.junit.Assume.assumeThat;
/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AwsShutdownAdvisedNotifierTest {

    private static final String INSTANCE_ID = "instanceId";
    private static final String TEST_TOPIC = "testTopic";

    @Mock
    private AmazonSNSClient snsClient;

    private ShutdownAdvisedNotifier shutdownAdvisedNotifier;

    @Before
    public void setUp() throws Exception {
        shutdownAdvisedNotifier = new AwsShutdownAdvisedNotifier(
                INSTANCE_ID,
                TEST_TOPIC,
                snsClient);
        assumeThat(shutdownAdvisedNotifier, allOf(
                hasProperty("instanceId", equalTo(INSTANCE_ID)),
                hasProperty("topicName", equalTo(TEST_TOPIC))));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldPublishShutdownAdvisedNotificationSuccessful() throws Exception {
        PublishResult publishResult = new PublishResult().withMessageId("messageId");
        final String reason = "Out of memory";
        final String shutdownAdvisedSubject = "Worker shutdown advised on " + INSTANCE_ID;
        final String shutdownAdvisedMessage = "Worker instance '" + INSTANCE_ID + "' needs restart: " + reason;
        final PublishRequest shutdownAdvisedRequest = new PublishRequest(TEST_TOPIC,
                shutdownAdvisedMessage, shutdownAdvisedSubject);
        doReturn(publishResult).when(snsClient).publish(shutdownAdvisedRequest);
        shutdownAdvisedNotifier.publishShutdownAdvisedNotification(reason);
        verify(snsClient, times(1)).publish(shutdownAdvisedRequest);
    }

    @Test
    public void shouldFailDuringPublishing() throws Exception {
        PublishResult publishResult = new PublishResult().withMessageId("messageId");
        final String reason = "Exception";
        final String shutdownAdvisedSubject = "Worker shutdown advised on " + INSTANCE_ID;
        final String shutdownAdvisedMessage = "Worker instance '" + INSTANCE_ID + "' needs restart: " + reason;
        final PublishRequest shutdownAdvisedRequest = new PublishRequest(TEST_TOPIC,
                shutdownAdvisedMessage, shutdownAdvisedSubject);
        doThrow(Exception.class).when(snsClient).publish(shutdownAdvisedRequest);
        shutdownAdvisedNotifier.publishShutdownAdvisedNotification(reason);
        // no test to do. Simple fact that the exception is not propagated beyond this method is enough.
    }

    @Test
    public void shouldHaveZeroInteractionsWithSnsClientNullTopicName() throws Exception {
        shutdownAdvisedNotifier = new AwsShutdownAdvisedNotifier(
                INSTANCE_ID,
                null,
                snsClient);
        shutdownAdvisedNotifier.publishShutdownAdvisedNotification("Error");
        verifyZeroInteractions(snsClient);
    }

    @Test
    public void shouldHaveZeroInteractionsWithSnsClientNullInstanceId() throws Exception {
        shutdownAdvisedNotifier = new AwsShutdownAdvisedNotifier(
                null,
                TEST_TOPIC,
                snsClient);
        shutdownAdvisedNotifier.publishShutdownAdvisedNotification("Error");
        verifyZeroInteractions(snsClient);
    }


}