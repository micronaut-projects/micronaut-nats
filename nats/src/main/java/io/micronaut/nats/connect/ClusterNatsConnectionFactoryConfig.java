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
package io.micronaut.nats.connect;

import javax.validation.constraints.NotNull;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;

/**
 * Allows configuration of multiple nats connections.
 * @author jgrimm
 * @since 1.0.0
 */
@Requires(property = ClusterNatsConnectionFactoryConfig.PREFIX)
@EachProperty(ClusterNatsConnectionFactoryConfig.PREFIX)
public class ClusterNatsConnectionFactoryConfig extends NatsConnectionFactoryConfig {

    public static final String PREFIX = "nats.servers";

    /**
     * Default constructor.
     * @param name The connection name
     */
    public ClusterNatsConnectionFactoryConfig(@Parameter("name") String name) {
        super(name);
    }

    /**
     * Sets the tls configuration.
     * @param tls The tls configuration
     */
    public void setTls(@NotNull DefaultTlsConfiguration tls) {
        super.setTls(tls);
    }

    /**
     * @see TLS
     */
    @ConfigurationProperties("tls")
    public static class DefaultTlsConfiguration extends TLS {

    }
}
