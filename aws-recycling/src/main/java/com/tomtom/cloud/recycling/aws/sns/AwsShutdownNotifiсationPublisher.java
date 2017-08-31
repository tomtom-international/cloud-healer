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
import com.tomtom.cloud.recycling.ShutdownNotifiсationPublisher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Publishes an SNS notification informing that a VM needs restart.
 */
@ManagedResource("Healer:component=worker,sub=shutdownAdvisedNotifier")
public class AwsShutdownNotifiсationPublisher implements ShutdownNotifiсationPublisher {

    /**
     * The aws id of the instance running the worker.
     */
    private final String instanceId;

    /**
     * The ARN of the topic where notifications will be published.
     */
    private final String topicName;

    /**
     * The client used for published notifications to Amazon SNS.
     */
    private final AmazonSNSClient snsClient;

    /**
     * Tells if we can publish notifications to Amazon SNS.
     */
    private final boolean canPublishNotifications;

    private static final Logger LOG = LoggerFactory.getLogger(AwsShutdownNotifiсationPublisher.class);

    /**
     * Constructor that should be used during system initialization,
     * preferably in a configuration bean.
     *
     * @param instanceId cloud instance id
     * @param topicName topic name
     * @param snsClient sns client instance
     */
    public AwsShutdownNotifiсationPublisher(
            final String instanceId,
            final String topicName,
            final AmazonSNSClient snsClient) {
        this.instanceId = instanceId;
        this.topicName = topicName;
        this.snsClient = snsClient;
        canPublishNotifications = canPublishNotifications();
    }


    @Override
    public void publishShutdownNotification(final String reason) {
        if (canPublishNotifications) {
            String shutdownAdvisedSubject = "VM self-termination triggired on " + instanceId;
            String shutdownAdvisedMessage = "VM instance '" + instanceId + "' needs to be replaced: " + reason;
            PublishRequest shutdownAdvisedRequest = new PublishRequest(topicName,
                    shutdownAdvisedMessage, shutdownAdvisedSubject);
            try {
                final PublishResult publishResult = snsClient.publish(shutdownAdvisedRequest);
                LOG.info("Published notification to Amazon SNS: {}", publishResult.getMessageId());
            } catch (Exception cause) {
                LOG.error("Could not publish notification to Amazon SNS because of: {}", cause.getMessage(), cause);
            }
        } else {
            LOG.info("notification " + reason + " was not published because topic or instance id were not configured");
        }
    }

    private boolean canPublishNotifications() {
        LOG.info("going to publish notifications to " + topicName);
        return snsClient != null && StringUtils.isNotEmpty(topicName) && StringUtils.isNotEmpty(instanceId);
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
