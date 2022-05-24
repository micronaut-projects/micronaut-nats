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
package io.micronaut.nats.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.messaging.annotation.MessageListener;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Class level annotation to indicate that a bean will be consumer of messages.
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

    /**
     * @return the queue of the consumer
     */
    String queue() default "";

    /**
     * Sets the client id of the Nats consumer. If not specified the client id is configured
     * to be the value of {@link io.micronaut.runtime.ApplicationConfiguration#getName()}.
     *
     * @return The client id
     * @since 3.1.0
     */
    String clientId() default "";

}
