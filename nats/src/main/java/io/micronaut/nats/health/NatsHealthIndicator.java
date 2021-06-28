/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.nats.health;

import java.util.Collections;
import java.util.Map;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.AbstractHealthIndicator;
import io.nats.client.Connection;
import jakarta.inject.Singleton;

/**
 * A {@link io.micronaut.management.health.indicator.HealthIndicator} for Nats.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Requires(property = HealthEndpoint.PREFIX + ".nats.enabled", notEquals = StringUtils.FALSE)
@Requires(beans = HealthEndpoint.class)
@Singleton
public class NatsHealthIndicator extends AbstractHealthIndicator<Map<String, Object>> {

    private final Connection connection;

    /**
     * Default constructor.
     *
     * @param connection The connection to query for details
     */
    public NatsHealthIndicator(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected Map<String, Object> getHealthInformation() {
        if (connection.getStatus() == Connection.Status.CONNECTED) {
            healthStatus = HealthStatus.UP;
            return Collections.singletonMap("servers", connection.getServers());
        } else {
            healthStatus = HealthStatus.DOWN;
            return Collections.emptyMap();
        }
    }

    @Override
    protected String getName() {
        return "nats";
    }

}
