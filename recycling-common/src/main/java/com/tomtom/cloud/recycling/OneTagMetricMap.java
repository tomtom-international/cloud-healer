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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concurrent map of atomic counters, using single-tag keys.
 * <p/>
 * Intended to simplify exposure of tagged metrics to Gallium.
 * <p/>
 * Optionally restricts the number of unique tags stored to a specified maximum.
 * This is useful when the tag is based on external input. In such a scenario a defensive upper bound might make sense.
 * <p/>
 * <i>Example: how old in seconds is the traffic for each feed that we support?</i>
 * <p/>
 * This can be represented as a OneTagMetricMap where the one tag is 'feed', like so:
 * <p/>
 * {
 *   {feed=FIN}=108,
 *   {feed=CZE}=51,
 *   {feed=AUT}=59,
 *   ..
 * }
 * <p/>
 * The Gallium library then knows to publish this as a set of numeric metrics which have a common
 * 'feed' tag, where the value of the 'feed' tag identifies which feed the value corresponds to.
 * The OpenTSDB backend is also then capable of aggregating all of the 'feed' values to give a
 * combined traffic freshness metric, e.g. average or max, across all feeds.
 *
 * @param <T> data type of metric tag.
 */
public class OneTagMetricMap<T> {

    private static final int NO_MAX_TAG_COUNT = -1;

    private final ConcurrentMap<Map<String, String>, AtomicLong> counts = new ConcurrentHashMap<>();

    private final String tagName;

    private final int maxTagCount;

    private final AtomicInteger tagCount = new AtomicInteger();

    /**
     * Creates a new, empty map.
     *
     * @param tagName name of the first tag
     */
    public OneTagMetricMap(final String tagName) {
        this(tagName, NO_MAX_TAG_COUNT);
    }

    /**
     * Creates a new, empty map, limiting the number of stored tags to the specified maximum.
     *
     * @param tagName name of the first tag
     * @param maxTagCount maximum number of tags to track
     */
    public OneTagMetricMap(final String tagName, final int maxTagCount) {
        this.tagName = tagName;
        this.maxTagCount = maxTagCount;
    }

    /**
     * Creates a new map filled with initial values.
     *
     * @param tagName       name of the first tag
     * @param initialValues initial values to fill the map.
     */
    public OneTagMetricMap(final String tagName, final Map<T, ? extends Number> initialValues) {
        this(tagName);
        for (Map.Entry<T, ? extends Number> entry : initialValues.entrySet()) {
            Map<String, String> key = getKey(entry.getKey());
            counts.put(key, new AtomicLong(entry.getValue().longValue()));
        }
    }

    public void increment(final T tag) {
        Map<String, String> key = getKey(tag);
        AtomicLong count = counts.get(key);
        if (count != null) {
            count.incrementAndGet();
        } else {
            create(key, 1);
        }
    }

    public void add(final T tag, final long value) {
        Map<String, String> key = getKey(tag);
        AtomicLong count = counts.get(key);
        if (count != null) {
            count.addAndGet(value);
        } else {
            create(key, value);
        }
    }

    public void set(final T tag, final long value) {
        Map<String, String> key = getKey(tag);
        AtomicLong count = counts.get(key);
        if (count != null) {
            count.set(value);
        } else {
            create(key, value);
        }
    }

    private void create(final Map<String, String> key, final long value) {
        if (maxTagCount == NO_MAX_TAG_COUNT || tagCount.get() < maxTagCount) {
            AtomicLong count = counts.putIfAbsent(key, new AtomicLong(value));
            if (count != null) {
                count.addAndGet(value);
            } else {
                tagCount.incrementAndGet();
            }
        }
    }

    public ConcurrentMap<Map<String, String>, ? extends Number> getCounts() {
        return counts;
    }

    private Map<String, String> getKey(final Object tag) {
        HashMap<String, String> key = new HashMap<>(1);
        key.put(tagName, tag.toString());
        return key;
    }
}
