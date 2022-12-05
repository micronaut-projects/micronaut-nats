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

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.nats.annotation.NatsConnection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;
import jakarta.inject.Singleton;

/**
 * handles new pull subscriptions.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Singleton
public class PullConsumerRegistry {

    private final BeanContext beanContext;

    public PullConsumerRegistry(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * create a new pull consumer subscription for the default connection.
     * the subscription is not managed by micronaut and needs to be closed manually
     *
     * @param subject              {@link String}
     * @param pullSubscribeOptions {@link PullSubscribeOptions}
     * @return jetStreamSubcription {@link JetStreamSubscription}
     * @throws JetStreamApiException in case of a jetstream error
     * @throws IOException           in case of a connection error
     */
    public JetStreamSubscription newPullConsumer(@NonNull String subject,
        @NonNull PullSubscribeOptions pullSubscribeOptions)
        throws JetStreamApiException, IOException {
        JetStream jetStream = beanContext.getBean(JetStream.class,
            Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION));
        return jetStream.subscribe(subject, pullSubscribeOptions);
    }

    /**
     * create a new pull consumer subscription for the default connection.
     * the subscription is not managed by micronaut and needs to be closed manually
     *
     * @param connectionName       {@link String}
     * @param subject              {@link String}
     * @param pullSubscribeOptions {@link PullSubscribeOptions}
     * @return jetStreamSubcription {@link JetStreamSubscription}
     * @throws JetStreamApiException in case of a jetstream error
     * @throws IOException           in case of a connection error
     */
    public JetStreamSubscription newPullConsumer(@NonNull String connectionName,
        @NonNull String subject, @NonNull PullSubscribeOptions pullSubscribeOptions)
        throws JetStreamApiException, IOException {

        JetStream jetStream =
            beanContext.getBean(JetStream.class, Qualifiers.byName(connectionName));
        return jetStream.subscribe(subject, pullSubscribeOptions);
    }

}
