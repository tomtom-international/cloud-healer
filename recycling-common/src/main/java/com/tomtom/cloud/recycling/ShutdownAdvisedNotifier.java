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

/**
 * Should be implemented in order to publish
 * shutdown notifications.
 */
public interface ShutdownAdvisedNotifier {

    /**
     * Publish shutdown notification.
     *
     * @param reason shutdown reason
     */
    void publishShutdownAdvisedNotification(String reason);

    /**
     * Return cloud instance id.
     *
     * @return instance id in cloud
     */
    String getInstanceId();

    /**
     * Return topic name.
     *
     * @return broker topic name
     */
    String getTopicName();

}
