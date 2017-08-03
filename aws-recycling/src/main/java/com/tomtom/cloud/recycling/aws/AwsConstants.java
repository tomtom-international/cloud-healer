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
package com.tomtom.cloud.recycling.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

/**
 * Constants to be used for AWS client resources.
 */
public final class AwsConstants {

    private static final Logger LOG = LoggerFactory.getLogger(AwsConstants.class);

    private AwsConstants() { }

    /**
     * Default timeout in seconds to be applied to AWS clients configuration options such as
     * connection timeout and socket timeout.
     */
    public static final int DEFAULT_AWS_CLIENT_TIMEOUT_SECONDS = 30;

    /**
     * AWS region clients will communicate with.
     */
    public static final Region AWS_REGION = getCurrentOrDefaultRegion();

    private static Region getCurrentOrDefaultRegion() {
        Region region = Regions.getCurrentRegion();
        LOG.info("Found AWS region: {}", region);

        if (region == null) {
            region = Region.getRegion(Regions.EU_WEST_1);
            LOG.info("No AWS region found. Using default: {}", region);
        }

        return region;
    }
}
