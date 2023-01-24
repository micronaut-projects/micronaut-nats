/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.nats.jetstream.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.nats.annotation.NatsConnection;
import jakarta.inject.Singleton;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Used to specify which key value bucket should be used.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Documented
@Retention(RUNTIME)
@Singleton
public @interface KeyValueStore {

    /**
     * The name of the key value store/bucket.
     * @return The name of the key value store.
     */
    String value() default "";

    /**
     * The nats connection to use.
     * @return The connection to use.
     * @see NatsConnection#connection()
     */
    @AliasFor(annotation = NatsConnection.class, member = "connection")
    String connection() default "";

    /**
     * This is an alias for value.
     * @return the bucket name to use.
     */
    @AliasFor(member = "value")
    String bucket() default "";

}
