/*
 * Copyright 2017-2019 original authors
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

package com.github.grimmjo.micronaut.nats.connect;

import java.io.IOException;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

/**
 * A factory for creating a connection to nats.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Factory
public class NatsConnectionFactory {

    /**
     * @param connectionFactory The factory to create the connection
     * @return The connection
     */
    @Bean(preDestroy = "close")
    @Singleton
    @EachBean(SingleNatsConnectionFactoryConfig.class)
    Connection connection(SingleNatsConnectionFactoryConfig connectionFactory) {
        try {
            Options.Builder builder = new Options.Builder();
            builder.connectionName(connectionFactory.getName());
            if (connectionFactory.getAddress().isPresent()) {
                builder.server(connectionFactory.getAddress().get());
            }
            return Nats.connect(builder.build());
        } catch (IOException | InterruptedException e) {
            throw new BeanInstantiationException("Error creating connection to nats", e);
        }

    }

}
