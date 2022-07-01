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
import java.util.Map;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * JetStreamFactory.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Factory
public class JetStreamFactory {

    private final BeanContext beanContext;

    @Inject
    public JetStreamFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * @param jetStreamConfiguration The jetstream configuration
     * @return The jetstream management
     * @throws IOException           in case of communication issue
     * @throws JetStreamApiException the request had an error related to the data
     */
    @Singleton
    @EachBean(JetStreamConfiguration.class)
    JetStreamManagement jetStreamManagement(JetStreamConfiguration jetStreamConfiguration)
        throws IOException, JetStreamApiException {
        final JetStreamManagement jetStreamManagement = getConnectionByName(
            jetStreamConfiguration.getConnectionName()).jetStreamManagement(
            jetStreamConfiguration.toJetStreamOptions());

        // initialize the given stream configurations
        for (Map.Entry<String, JetStreamConfiguration.StreamConfiguration> streamEntry :
            jetStreamConfiguration.getStreams().entrySet()) {
            final StreamConfiguration streamConfiguration =
                streamEntry.getValue().toStreamConfiguration(streamEntry.getKey());
            if (jetStreamManagement.getStreamNames().contains(streamEntry.getKey())) {
                StreamInfo streamInfo = jetStreamManagement.getStreamInfo(streamEntry.getKey());
                if (!streamInfo.getConfiguration().equals(streamConfiguration)) {
                    jetStreamManagement.updateStream(streamConfiguration);
                }
            } else {
                jetStreamManagement.addStream(streamConfiguration);
            }
        }

        return jetStreamManagement;
    }

    /**
     * @param jetStreamConfiguration The jetstream configuration
     * @return The jetstream
     * @throws IOException in case of communication issue
     */
    @Singleton
    @EachBean(JetStreamConfiguration.class)
    JetStream jetStream(JetStreamConfiguration jetStreamConfiguration)
        throws IOException {
        return getConnectionByName(jetStreamConfiguration.getConnectionName())
            .jetStream(jetStreamConfiguration.toJetStreamOptions());
    }

    private Connection getConnectionByName(String connectionName) {
        return beanContext.findBean(Connection.class, Qualifiers.byName(connectionName))
                          .orElseThrow(() -> new IllegalStateException(
                              "No nats connection found for " + connectionName));
    }
}
