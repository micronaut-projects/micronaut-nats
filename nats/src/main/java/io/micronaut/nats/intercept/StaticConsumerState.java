/*
 * Copyright 2017-2022 original authors
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

import java.util.Set;

import io.micronaut.core.annotation.Internal;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Subscription;

/**
 * Manages the consumer state for nats and jetstream.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Internal
public final class StaticConsumerState {

    private final String clientId;

    private final Set<Subscription> subscriptions;

    private final Dispatcher dispatcher;

    private final Connection connection;

    /**
     * default constructor.
     *
     * @param clientId      the client id
     * @param subscriptions the subscriptions of the consumer
     * @param dispatcher    the dispatcher of the consumer
     * @param connection    the connection of the consumer
     */
    public StaticConsumerState(String clientId, Set<Subscription> subscriptions,
        Dispatcher dispatcher, Connection connection) {
        this.clientId = clientId;
        this.subscriptions = subscriptions;
        this.dispatcher = dispatcher;
        this.connection = connection;
    }

    public String getClientId() {
        return clientId;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection.getStatus() != Connection.Status.CLOSED) {
            connection.closeDispatcher(dispatcher);
        }
    }
}
