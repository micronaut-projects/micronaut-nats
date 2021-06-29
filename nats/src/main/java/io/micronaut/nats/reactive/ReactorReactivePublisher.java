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
package io.micronaut.nats.reactive;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Internal;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * @author jgrimm
 */

@Internal
@EachBean(Connection.class)
public class ReactorReactivePublisher implements ReactivePublisher {

    private final Connection connection;

    /**
     * Constructor.
     * @param connection The given connection
     */
    public ReactorReactivePublisher(@Parameter Connection connection) {
        this.connection = connection;
    }

    @Override
    public Publisher<Void> publish(PublishState publishState) {
        return getConnection().flatMap(con -> publishInternal(publishState, con));
    }

    private Mono<Void> publishInternal(PublishState publishState, Connection con) {
        return Mono.create(subscriber -> {
            con.publish(publishState.getSubject(), publishState.getBody());
            subscriber.success();
        });
    }

    @Override
    public Publisher<Message> publishAndReply(PublishState publishState) {
        return getConnection()
                .flatMap(con -> Mono.fromFuture(con.request(publishState.getSubject(), publishState.getBody())));
    }

    private Mono<Connection> getConnection() {
        return Mono.just(connection);
    }
}
