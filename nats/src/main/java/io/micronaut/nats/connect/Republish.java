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
package io.micronaut.nats.connect;

import io.micronaut.core.annotation.NonNull;

/**
 * Manages the republish information of a stream or kv store.
 *
 * @author Joachim Grimm
 * @since 4.1.0
 */
public abstract class Republish {

    @NonNull
    private String source;

    @NonNull
    private String destination;

    private boolean headersOnly;

    /**
     * Builder of the Republish object.
     *
     * @return builder
     */
    io.nats.client.api.Republish build() {
        return io.nats.client.api.Republish.builder()
            .source(source)
            .destination(destination)
            .headersOnly(headersOnly)
            .build();
    }

    /**
     * Source.
     *
     * @param source {@link String}
     */
    public void setSource(@NonNull String source) {
        this.source = source;
    }

    /**
     * Destination.
     *
     * @param destination {@link String}
     */
    public void setDestination(@NonNull String destination) {
        this.destination = destination;
    }

    /**
     * HeadersOnly.
     *
     * @param headersOnly boolean
     */
    public void setHeadersOnly(boolean headersOnly) {
        this.headersOnly = headersOnly;
    }
}
