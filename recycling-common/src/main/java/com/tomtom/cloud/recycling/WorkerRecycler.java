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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * VM Recycler.
 */
public class WorkerRecycler {

    /**
     * The client used to manage cloud scaling group instances.
     */
    private final CloudAdapter cloudAdapter;

    private volatile boolean instanceRecycleCalled = false;

    public WorkerRecycler(final CloudAdapter cloudAdapter) {
        this.cloudAdapter = cloudAdapter;
    }

    /**
     * This method is only meant to be called once and to eventually cause the instance to be recycled
     * (terminated and replaced with a new instance).
     */
    public synchronized void recycle() {
        if (isValidSetup() && !instanceRecycleCalled) {
            final WorkerRecyclerThread recycleThread = new WorkerRecyclerThread(cloudAdapter);
            recycleThread.start();
            instanceRecycleCalled = true;
        }
    }

    public boolean isValidSetup() {
        return StringUtils.isNotEmpty(cloudAdapter.getInstanceId());
    }
}

/**
 * Thread for triggering the recycling of the current Worker instance.
 * - Create and apply a scaling policy to double the number of instances in the current instance auto scaling group.
 * - Sleep and wait until instances that are associated to the same load balancer as the current instance are healthy.
 * - Terminate the current instance, decrease the desired capacity and remove the scaling policy.
 */
final class WorkerRecyclerThread extends Thread {

    /**
     * Maximum number of checks that instances that are associated to the same load balancer
     * as the current instance are healthy.
     */
    private static final int MAX_NUMBER_CHECKS = 60;

    /**
     * Initial period to wait before checking the instance state of all instances associated to an ELB.
     */
    private static final long INITIAL_SLEEP_MILLIS = 240000;

    /**
     * Period to wait between instances state checks.
     */
    private static final long SLEEP_MILLIS = 60000;

    private final CloudAdapter cloudAdapter;
    private ThreadContext threadContext = ThreadContext.SYSTEM;

    private static final Logger LOG = LoggerFactory.getLogger(WorkerRecyclerThread.class);

    WorkerRecyclerThread(final CloudAdapter cloudAdapter) {
        super("Shutdown Advised Handler Thread");
        this.cloudAdapter = cloudAdapter;
    }

    @Override
    public void run() {
        try {
            // Double the number of instances in the current instance auto scaling group.
            cloudAdapter.doubleAutoScalingGroupSize();

            // Sleep and wait until all instances that are associated to the same load balancer as the current
            // instance are healthy.
            waitForHealthyInstances();

            // Terminate the current instance and decrease desired capacity.
            cloudAdapter.terminateCurrentInstance();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void waitForHealthyInstances() throws MonitoringException {
        try {
            threadContext.sleep(INITIAL_SLEEP_MILLIS);

            for (int i = 0; i < MAX_NUMBER_CHECKS; i++) {
                threadContext.sleep(SLEEP_MILLIS);

                // if any of them are still unhealthy we need to wait
                if (cloudAdapter.areAllInstancesHealthy()) {
                    LOG.info("All load balancer instances are healthy.");
                    break;
                }
            }
        } catch (Exception cause) {
            final String message = "Error checking load balancer instances health: " + cause.getMessage();
            throw new MonitoringException(message, cause);
        }
    }

    void setThreadContext(final ThreadContext threadContext) {
        this.threadContext = threadContext;
    }
}
