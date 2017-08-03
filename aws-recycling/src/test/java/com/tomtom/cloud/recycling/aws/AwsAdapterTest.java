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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AwsAdapterTest {

    private static final int TEST_TIMEOUT = 42;

    private static final String TEST_INSTANCE_ID = "testInstanceId";

    private AwsAdapter testedAdapter;

    @Before
    public void setup() {
        testedAdapter = new AwsAdapter(TEST_INSTANCE_ID, TEST_TIMEOUT);
    }

    @Test
    public void shouldHaveInstanceIdEqualToTestInstanceId() {
        assertEquals(TEST_INSTANCE_ID, testedAdapter.getInstanceId());
    }

    @Test
    public void shouldHaveTimeoutEqualToTestTimeOut() {
        assertEquals(TEST_TIMEOUT, testedAdapter.getTimeoutSeconds());
    }

}
