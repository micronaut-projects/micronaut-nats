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
package io.micronaut.nats.jetstream.intercept;

import java.util.Optional;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.nats.intercept.StaticPublisherState;
import io.micronaut.nats.jetstream.annotation.JetStreamClient;
import io.micronaut.nats.jetstream.reactive.ReactivePublisher;

/**
 * Stores the static state for publishing messages with
 * {@link JetStreamClient}.
 *
 * @author jgrimm
 * @since 4.0.0
 */
@Internal
class JetStreamPublisherState extends StaticPublisherState {

    private final Optional<Argument> publishOptions;

    private final ReactivePublisher reactivePublisher;

    /**
     * Default constructor.
     *
     * @param publisherState    The publisher context from nats
     * @param reactivePublisher The reactive publisher
     * @param publishOptions    The argument representing the publish options
     */
    JetStreamPublisherState(StaticPublisherState publisherState,
        ReactivePublisher reactivePublisher,
        Optional<Argument> publishOptions) {
        super(publisherState);
        this.publishOptions = publishOptions;
        this.reactivePublisher = reactivePublisher;
    }

    /**
     * @return The PublishOptions argument
     */
    Optional<Argument> getPublishOptions() {
        return publishOptions;
    }

    /**
     * @return the jetstream reactive publisher
     */
    ReactivePublisher getJetstreanReactivePublisher() {
        return reactivePublisher;
    }
}
