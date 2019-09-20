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
 * A factory for creating a connection to nats
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
