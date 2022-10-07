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

import java.io.Closeable;
import java.io.IOException;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
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
@EachBean(JetStream.class)
@Singleton
public class PullConsumerRegistry implements Closeable {

    private final JetStream jetStream;

    public PullConsumerRegistry(@Parameter JetStream jetStream) {
        this.jetStream = jetStream;
    }

    /**
     * create a new pull consumer subscription.
     *
     * @param subject              {@link String}
     * @param pullSubscribeOptions {@link PullSubscribeOptions}
     * @return jetStreamSubcription {@link JetStreamSubscription}
     * @throws JetStreamApiException in case of a jetstream error
     * @throws IOException           in case of a connection error
     */
    public JetStreamSubscription newConsumer(@NonNull String subject,
        @NonNull PullSubscribeOptions pullSubscribeOptions)
        throws JetStreamApiException, IOException {
        return jetStream.subscribe(subject, pullSubscribeOptions);
    }

    @Override
    public void close() throws IOException {

    }
}
