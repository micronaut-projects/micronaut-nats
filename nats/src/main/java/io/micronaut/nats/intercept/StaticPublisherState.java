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

package io.micronaut.nats.intercept;

import java.util.Optional;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.nats.reactive.ReactivePublisher;
import io.micronaut.nats.serdes.NatsMessageSerDes;

/**
 * Stores the static state for publishing messages with
 * {@link io.micronaut.nats.annotation.NatsClient}.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Internal
class StaticPublisherState {

    private final String subject;
    private final Argument bodyArgument;
    private final ReturnType<?> returnType;
    private final String connection;
    private final NatsMessageSerDes<?> serDes;
    private final Argument<?> dataType;
    private final ReactivePublisher reactivePublisher;
    private final boolean reactive;

    /**
     * Default constructor.
     * @param subject      The subject to publish to
     * @param bodyArgument The argument representing the body
     * @param returnType   The return type of the method
     * @param connection   The connection name
     * @param serDes       The body serializer
     * @param reactivePublisher The reactive publisher
     */
    StaticPublisherState(String subject, Argument bodyArgument, ReturnType<?> returnType, String connection,
            NatsMessageSerDes<?> serDes, ReactivePublisher reactivePublisher) {
        this.subject = subject;
        this.bodyArgument = bodyArgument;
        this.connection = connection;
        this.serDes = serDes;
        this.reactivePublisher = reactivePublisher;
        Class<?> javaReturnType = returnType.getType();
        this.reactive = Publishers.isConvertibleToPublisher(javaReturnType);
        if (this.reactive) {
            this.dataType = returnType.getFirstTypeVariable()
                    .orElse(Argument.VOID);
        } else {
            this.dataType = returnType.asArgument();
        }
        this.returnType = returnType;
    }

    /**
     * @return The subject
     */
    Optional<String> getSubject() {
        return Optional.ofNullable(subject);
    }

    /**
     * @return The body argument
     */
    Argument getBodyArgument() {
        return bodyArgument;
    }

    /**
     * @return The type of data being requested
     */
    Argument<?> getDataType() {
        return dataType;
    }

    /**
     * @return The return type
     */
    ReturnType<?> getReturnType() {
        return returnType;
    }

    /**
     * @return the connection name
     */
    String getConnection() {
        return connection;
    }

    /**
     * @return The serializer
     */
    NatsMessageSerDes<Object> getSerDes() {
        return (NatsMessageSerDes) serDes;
    }

    /**
     * @return True if the method returns a reactive type
     */
    boolean isReactive() {
        return reactive;
    }

    /**
     * @return The reactive publisher
     */
    ReactivePublisher getReactivePublisher() {
        return reactivePublisher;
    }

}
