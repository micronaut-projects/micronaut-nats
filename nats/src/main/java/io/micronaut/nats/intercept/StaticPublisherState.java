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
package io.micronaut.nats.intercept;

import java.util.Optional;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.nats.reactive.ReactivePublisher;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.nats.client.impl.Headers;

/**
 * Stores the static state for publishing messages with
 * {@link io.micronaut.nats.annotation.NatsClient}.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Internal
public class StaticPublisherState {

    private final String subject;

    private final Argument<?> bodyArgument;

    private final Headers headers;

    private final String connection;

    private final NatsMessageSerDes<?> serDes;

    private final Argument<?> dataType;

    private final ReactivePublisher reactivePublisher;

    /**
     * Default constructor.
     *
     * @param subject           The subject to publish to
     * @param bodyArgument      The argument representing the body
     * @param methodHeaders     The methods headers
     * @param returnType        The return type of the method
     * @param connection        The connection to use
     * @param serDes            The body serializer
     * @param reactivePublisher The reactive publisher
     */
    protected StaticPublisherState(String subject, Argument<?> bodyArgument, Headers methodHeaders,
        ReturnType<?> returnType, String connection,
        NatsMessageSerDes<?> serDes, ReactivePublisher reactivePublisher) {
        this.subject = subject;
        this.bodyArgument = bodyArgument;
        this.headers = methodHeaders;
        this.serDes = serDes;
        this.connection = connection;
        this.reactivePublisher = reactivePublisher;
        Class<?> javaReturnType = returnType.getType();
        boolean reactive = Publishers.isConvertibleToPublisher(javaReturnType);
        if (reactive) {
            this.dataType = returnType.getFirstTypeVariable()
                                      .orElse(Argument.VOID);
        } else {
            this.dataType = returnType.asArgument();
        }
    }

    protected StaticPublisherState(StaticPublisherState other) {
        this.subject = other.subject;
        this.bodyArgument = other.bodyArgument;
        this.headers = other.headers;
        this.serDes = other.serDes;
        this.reactivePublisher = other.reactivePublisher;
        this.dataType = other.dataType;
        this.connection = other.connection;
    }

    /**
     * @return The subject
     */
    public Optional<String> getSubject() {
        return Optional.ofNullable(subject);
    }

    /**
     * @return The body argument
     */
    public Argument getBodyArgument() {
        return bodyArgument;
    }

    /**
     * @return the method headers
     */
    public Headers getHeaders() {
        return new Headers(headers);
    }

    /**
     * @return The type of data being requested
     */
    public Argument getDataType() {
        return dataType;
    }

    /**
     * @return The serializer
     */
    public NatsMessageSerDes<Object> getSerDes() {
        return (NatsMessageSerDes) serDes;
    }

    /**
     * @return The reactive publisher
     */
    public ReactivePublisher getReactivePublisher() {
        return reactivePublisher;
    }

    /**
     * @return the connection name
     */
    public String getConnection() {
        return connection;
    }

}
