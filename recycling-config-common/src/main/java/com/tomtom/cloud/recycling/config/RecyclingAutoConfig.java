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
package com.tomtom.cloud.recycling.config;

import com.tomtom.cloud.recycling.CloudAdapter;
import com.tomtom.cloud.recycling.MetricsPublisher;
import com.tomtom.cloud.recycling.MetricsUpdater;
import com.tomtom.cloud.recycling.MetricsUpdaterImpl;
import com.tomtom.cloud.recycling.WorkerRecycler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Autoconfiguration for cloud recycling.
 */
@ConditionalOnExpression("'${cloud.aws.enabled}'=='true' or '${cloud.azure.enabled}'=='true'")
@Configuration
public class RecyclingAutoConfig {
    private static final long DEFAULT_SLEEP_TIME_SECONDS = 60;

    @Value("${cloud.monitoring.sleep:" + DEFAULT_SLEEP_TIME_SECONDS + "}")
    private int sleepIntervalSeconds;

    @Value("${cloud.monitoring.enabled:false}")
    private boolean enabled;

    @Bean
    public MetricsUpdater metricsUpdater(final MetricsPublisher publisher) {
        return new MetricsUpdaterImpl(publisher, sleepIntervalSeconds, enabled);
    }

    @Bean
    public WorkerRecycler workerRecycler(final CloudAdapter cloudAdapter) {
        return new WorkerRecycler(cloudAdapter);
    }
}
