/*
 * Copyright 2017-2018 original authors
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

package com.github.grimmjo.micronaut.nats.intercept;

import com.github.grimmjo.micronaut.nats.serdes.NatsMessageSerDes;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;

/**
 * Stores the static state for publishing messages with
 * {@link com.github.grimmjo.micronaut.nats.annotation.NatsClient}.
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

    /**
     * Default constructor.
     * @param subject      The subject to publish to
     * @param bodyArgument The argument representing the body
     * @param returnType   The return type of the method
     * @param connection   The connection name
     * @param serDes       The body serializer
     */
    StaticPublisherState(String subject, Argument bodyArgument, ReturnType<?> returnType, String connection,
            NatsMessageSerDes<?> serDes) {
        this.subject = subject;
        this.bodyArgument = bodyArgument;
        this.dataType = returnType.asArgument();
        this.returnType = returnType;
        this.connection = connection;
        this.serDes = serDes;
    }

    /**
     * @return The subject
     */
    String getSubject() {
        return subject;
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

    String getConnection() { return connection;}

    /**
     * @return The serializer
     */
    NatsMessageSerDes<Object> getSerDes() {
        return (NatsMessageSerDes) serDes;
    }

}
