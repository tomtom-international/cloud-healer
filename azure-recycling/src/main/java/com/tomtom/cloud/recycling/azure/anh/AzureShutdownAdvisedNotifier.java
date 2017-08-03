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

import java.nio.charset.StandardCharsets;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import com.tomtom.cloud.recycling.ShutdownAdvisedNotifier;

/**
 * Publish an Azure Notification Hub notification informing that a Worker needs restart.
 */
@ManagedResource("Healer:component=worker,sub=shutdownAdvisedNotifier")
public class AzureShutdownAdvisedNotifier implements ShutdownAdvisedNotifier {

    private final String instanceId;

    private final String topicName;

    private final EventHubClient client;

    /**
     * Constructor that should be used during system initialization,
     * preferably in a configuration bean.
     *
     * @param instanceId cloud instance id
     * @param topicName  topic name
     * @param client     event hub client
     */
    public AzureShutdownAdvisedNotifier(String instanceId, String topicName, EventHubClient client) {
        this.instanceId = instanceId;
        this.topicName = topicName;
        this.client = client;
    }


    @Override
    public void publishShutdownAdvisedNotification(String reason) {
        if (canPublishNotifications()) {
            final String shutdownAdvisedSubject = "Worker shutdown advised on " + instanceId;
            final String shutdownAdvisedMessage = "Worker instance '" + instanceId + "' needs restart: " + reason;
            EventData event = new EventData(
                    String.format("%s%n%s", shutdownAdvisedSubject, shutdownAdvisedMessage)
                            .getBytes(StandardCharsets.UTF_8));
            client.send(event);
        }
    }

    private boolean canPublishNotifications() {
        return client != null && StringUtils.isNotEmpty(topicName) && StringUtils.isNotEmpty(instanceId);
    }

    @ManagedAttribute
    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @ManagedAttribute
    @Override
    public String getTopicName() {
        return topicName;
    }
}
