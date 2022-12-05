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

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Internal;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Internal
@EachBean(JetStream.class)
class ReactorReactivePublisher implements ReactivePublisher {

    private final Mono<JetStream> jetStream;

    /**
     * Constructor.
     *
     * @param jetStream The given connection
     */
    public ReactorReactivePublisher(@Parameter JetStream jetStream) {
        this.jetStream = Mono.just(jetStream);
    }

    @Override
    public Publisher<PublishAck> publish(Message message) {
        return jetStream.flatMap(js -> Mono.fromFuture(js.publishAsync(message)));
    }

    @Override
    public Publisher<PublishAck> publish(Message message, PublishOptions options) {
        return jetStream.flatMap(js -> Mono.fromFuture(js.publishAsync(message, options)));
    }
}
