package com.github.grimmjo.micronaut.nats.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Scope;
import javax.inject.Singleton;

import com.github.grimmjo.micronaut.nats.intercept.NatsIntroductionAdvice;
import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Type;
import io.micronaut.retry.annotation.Recoverable;

/**
 * An introduction advice that automatically implemnts interfaces and abstract classes and publishes nats.io messages
 * @author jgrimm
 * @see NatsIntroductionAdvice
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Scope
@Introduction
@Type(NatsIntroductionAdvice.class)
@Recoverable
@Singleton
public @interface NatsClient {

    /**
     * @see NatsConnection#connection()
     * @return The connection to use
     */
    @AliasFor(annotation = NatsConnection.class, member = "connection")
    String connection() default "";

}
