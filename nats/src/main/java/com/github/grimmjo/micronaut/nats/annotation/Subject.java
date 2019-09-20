package com.github.grimmjo.micronaut.nats.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.context.annotation.AliasFor;

/**
 * Used to specify which subject should be used for messages
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface Subject {

    /**
     * @return The subject to subscribe to.
     */
    String value();

    /**
     * @see NatsConnection#connection()
     * @return The connection to use
     */
    @AliasFor(annotation = NatsConnection.class, member = "connection")
    String connection() default "";
}
