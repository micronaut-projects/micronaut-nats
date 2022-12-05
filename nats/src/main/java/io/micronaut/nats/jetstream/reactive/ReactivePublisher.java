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
package io.micronaut.nats.jetstream.reactive;

import io.nats.client.Message;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import org.reactivestreams.Publisher;

/**
 * A generic contract publishing message reactively.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
public interface ReactivePublisher {

    /**
     * Publish the message with the provided arguments and return
     * a reactive type that completes successfully when the message
     * is published.
     *
     * @param message The message to publish
     * @return the publisher
     */
    Publisher<PublishAck> publish(Message message);

    /**
     * Publish the message with the provided arguments and return
     * a reactive type that completes successfully when the message
     * is published.
     *
     * @param message The message to publish
     * @param options the publish options
     * @return the publisher
     */
    Publisher<PublishAck> publish(Message message, PublishOptions options);

}
