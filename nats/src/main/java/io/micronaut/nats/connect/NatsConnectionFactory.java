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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.scheduling.TaskExecutors;
import io.nats.client.Connection;
import io.nats.client.Nats;

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
     * @param executorService   The messaging executer service
     * @return The connection
     */
    @Bean(preDestroy = "close")
    @Singleton
    @EachBean(NatsConnectionFactoryConfig.class)
    Connection connection(NatsConnectionFactoryConfig connectionFactory,
            @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService) {
        try {
            return Nats.connect(connectionFactory.toOptionsBuilder().executor(executorService).build());
        } catch (IOException | InterruptedException | GeneralSecurityException e) {
            throw new BeanInstantiationException("Error creating connection to nats", e);
        }

    }

}
