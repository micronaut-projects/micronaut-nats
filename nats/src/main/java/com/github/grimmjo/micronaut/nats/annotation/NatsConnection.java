package com.github.grimmjo.micronaut.nats.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.github.grimmjo.micronaut.nats.connect.SingleNatsConnectionFactoryConfig;

/**
 * Stores the options surrounding a Nats connection.
 *
 * @author Joachim Grimm
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD})
public @interface NatsConnection {

    String DEFAULT_CONNECTION = SingleNatsConnectionFactoryConfig.DEFAULT_NAME;

    /**
     * @return The connection to use
     */
    String connection() default DEFAULT_CONNECTION;
}
