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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Uses a metric publisher to publish metrics at defined intervals.
 * The publishing can be enabled and disabled and is disabled by default.
 */
@ManagedResource("Healer:component=worker,sub=cloudWatchMetricsUpdater")
public class MetricsUpdaterImpl implements MetricsUpdater {

    private final MetricsPublisher publisher;

    private final int sleepIntervalSeconds;

    private volatile boolean enabled;

    private ScheduledExecutorService executor;


    /**
     * Constructor that should be used during system initialization, preferably in a configuration bean.
     *
     * @param publisher            metrics published
     * @param sleepIntervalSeconds sleep interval between publishing
     * @param enabled              true to enable it
     */
    public MetricsUpdaterImpl(final MetricsPublisher publisher,
                              final int sleepIntervalSeconds,
                              final boolean enabled) {
        this.publisher = publisher;
        this.sleepIntervalSeconds = sleepIntervalSeconds;
        this.enabled = enabled;
    }

    /**
     * Start publisher after object creation.
     */
    @ManagedOperation
    @PostConstruct
    @Override
    public synchronized void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(
                    new MetricsUpdaterThread(this, publisher),
                    0, sleepIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Stop publisher when the shutdown hook is invoked.
     */
    @ManagedOperation
    @PreDestroy
    @Override
    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @ManagedAttribute
    @Override
    public synchronized boolean isEnabled() {
        return enabled;
    }

    @ManagedAttribute
    @Override
    public synchronized void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @ManagedAttribute
    @Override
    public boolean isRunning() {
        return executor != null;
    }

    @ManagedAttribute
    @Override
    public int getSleepIntervalSeconds() {
        return sleepIntervalSeconds;
    }
}

/**
 * The Thread class that allows the MetricsUpdaterImpl to run on a separate thread.
 */
final class MetricsUpdaterThread extends Thread {

    private final MetricsPublisher publisher;
    private final MetricsUpdater updater;
    private static final Logger LOG = LoggerFactory.getLogger(MetricsUpdaterThread.class);

    MetricsUpdaterThread(final MetricsUpdater updater,
                         final MetricsPublisher publisher) {
        super("Metrics Publisher thread");
        setDaemon(true);
        this.publisher = publisher;
        this.updater = updater;
    }

    @Override
    public void run() {
        try {
            if (updater.isEnabled()) {
                publisher.publish();
            }
        } catch (Exception e) {
            LOG.error("Could not publish metrics: {}", e.getMessage(), e);
        }
    }
}
