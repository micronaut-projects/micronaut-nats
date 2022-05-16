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
package io.micronaut.nats;

import java.util.Set;

import io.micronaut.core.annotation.NonNull;
import io.nats.client.Consumer;
import io.nats.client.Subscription;

/**
 * A registry for created Nats consumers.
 *
 * @author jgrimm
 */
public interface ConsumerRegistry {

    /**
     * Returns the subscription by given id.
     *
     * @param id {@link String} The id of the listener
     * @return The consumer
     * @throws IllegalArgumentException If no listener exists for the given ID
     */
    @NonNull
    Consumer getConsumer(@NonNull String id);

    /**
     * The IDs of the available consumers.
     *
     * @return The consumers
     */
    @NonNull
    Set<String> getConsumerIds();

    /**
     * Returns a managed Consumer's subscriptions.
     *
     * @param id The id of the producer.
     * @return The consumer subscription
     * @throws IllegalArgumentException If no consumer exists for the given ID
     */
    @NonNull
    Set<Subscription> getConsumerSubscription(@NonNull String id);

}
