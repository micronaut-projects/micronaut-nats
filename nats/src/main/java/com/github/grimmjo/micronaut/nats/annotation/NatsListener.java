package com.github.grimmjo.micronaut.nats.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.messaging.annotation.MessageListener;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Class level annotation to indicate that a bean will be consumer of messages
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.TYPE})
@MessageListener
public @interface NatsListener {

    /**
     * @see NatsConnection#connection()
     * @return The connection to use
     */
    @AliasFor(annotation = NatsConnection.class, member = "connection")
    String connection() default "";
}
