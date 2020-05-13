/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.nats.reactive;

import io.nats.client.Message;
import org.reactivestreams.Publisher;

/**
 * A generic contract publishing message reactively.
 * @author jgrimm
 */
public interface ReactivePublisher {

    /**
     * Publish the message with the provided arguments and return
     * a reactive type that completes successfully when the message
     * is published.
     *
     * @param publishState The publishing data
     * @return the publisher
     */
    Publisher<Void> publish(PublishState publishState);

    /**
     * Publish the message with the provided arguments and return
     * a reactive type that completes successfully when the reply
     * is received from the reply to queue.
     *
     * @param publishState The publishing data
     *
     * @return The publisher of the received reply
     */

    Publisher<Message> publishAndReply(PublishState publishState);
}
