/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.connect;

import java.time.Duration;

/**
 * Manages the consumer limits of a stream.
 *
 * @author Joachim Grimm
 * @since 4.1.0
 */
public abstract class ConsumerLimits {

    private Duration inactiveThreshold;
    private Long maxAckPending;

    /**
     * Builder of the Republish object.
     *
     * @return builder
     */
    io.nats.client.api.ConsumerLimits build() {
        return io.nats.client.api.ConsumerLimits.builder()
            .inactiveThreshold(inactiveThreshold)
            .maxAckPending(maxAckPending)
            .build();
    }

    /**
     * Inactive threshold.
     * @return duration
     */
    public Duration getInactiveThreshold() {
        return inactiveThreshold;
    }

    /**
     * Inactive threshold.
     * @param inactiveThreshold {@link Duration}
     */
    public void setInactiveThreshold(Duration inactiveThreshold) {
        this.inactiveThreshold = inactiveThreshold;
    }

    /**
     * Maximal acknowledgments pending.
     * @return maxAckPending
     */
    public Long getMaxAckPending() {
        return maxAckPending;
    }

    /**
     * Maximal acknowledgments pending.
     * @param maxAckPending {@link Long}
     */
    public void setMaxAckPending(Long maxAckPending) {
        this.maxAckPending = maxAckPending;
    }
}
