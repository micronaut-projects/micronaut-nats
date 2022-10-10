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
package io.micronaut.nats.jetstream;

import java.io.IOException;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PushSubscribeOptions;

/**
 * A registry for jetstream push consumers.
 *
 * @author Joachim grimm
 * @since 4.0.0
 */
public interface PushConsumerRegistry {

    /**
     * Create a new push consumer with the default connection.
     *
     * @param subject              {@link String}
     * @param pushSubscribeOptions {@link PushSubscribeOptions}
     * @param queue                {@link String} optional
     * @return subscription {@link JetStreamSubscription}
     * @throws JetStreamApiException in case of a jetstream error
     * @throws IOException           in case of a connection error
     */
    JetStreamSubscription newSubscription(@NonNull String subject,
        @NonNull PushSubscribeOptions pushSubscribeOptions, @Nullable String queue)
        throws JetStreamApiException, IOException;

    /**
     * Create a new push consumer from the given connection name.
     *
     * @param connectionName       {@link String}
     * @param subject              {@link String}
     * @param pushSubscribeOptions {@link PushSubscribeOptions}
     * @param queue                {@link String} optional
     * @return subscription {@link JetStreamSubscription}
     * @throws JetStreamApiException in case of a jetstream error
     * @throws IOException           in case of a connection error
     */
    JetStreamSubscription newSubscription(@NonNull String connectionName, @NonNull String subject,
        @NonNull PushSubscribeOptions pushSubscribeOptions, @Nullable String queue)
        throws JetStreamApiException, IOException;

}
