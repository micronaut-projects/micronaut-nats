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

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Retain;
import io.micronaut.nats.connect.NatsConnectionFactoryConfig;
import io.micronaut.scheduling.executor.ExecutorConfiguration;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * JetStreamFactory.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Factory
public class JetStreamFactory {

    /**
     * Creates the factory. It holds nothing bound to the context: the JetStream beans receive their connection, so
     * that development mode can retain them, with the connection, across a restart.
     *
     * @since 5.2.0
     */
    @Inject
    public JetStreamFactory() {
    }

    /**
     * @param beanContext The bean context, not used
     * @deprecated The JetStream beans receive their connection, use {@link #JetStreamFactory()}
     */
    @Deprecated(since = "5.2.0")
    public JetStreamFactory(BeanContext beanContext) {
        this();
    }

    /**
     * @param config The jetstream configuration
     * @param connection The connection of the configuration
     * @return The jetstream management
     * @throws IOException in case of communication issue
     */
    @Singleton
    @EachBean(NatsConnectionFactoryConfig.class)
    @Retain(invalidatedBy = {NatsConnectionFactoryConfig.PREFIX, ExecutorConfiguration.PREFIX_CONSUMER})
    JetStreamManagement jetStreamManagement(NatsConnectionFactoryConfig config, @Parameter Connection connection) throws IOException {
        if (config.getJetstream() != null) {
            return connection.jetStreamManagement(
                    config.getJetstream().toJetStreamOptions());
        }
        return null;
    }

    /**
     * @param config The jetstream configuration
     * @param connection The connection of the configuration
     * @return The jetstream
     * @throws IOException           in case of communication issue
     * @throws JetStreamApiException the request had an error related to the data
     */
    @Singleton
    @EachBean(NatsConnectionFactoryConfig.class)
    @Retain(invalidatedBy = {NatsConnectionFactoryConfig.PREFIX, ExecutorConfiguration.PREFIX_CONSUMER})
    JetStream jetStream(NatsConnectionFactoryConfig config, @Parameter Connection connection) throws IOException, JetStreamApiException {
        if (config.getJetstream() != null) {
            createOrUpdateStreams(config, connection);

            return connection.jetStream(config.getJetstream().toJetStreamOptions());
        }
        return null;
    }

    private void createOrUpdateStreams(NatsConnectionFactoryConfig config, Connection connection) throws IOException, JetStreamApiException {
        final JetStreamManagement jetStreamManagement = connection.jetStreamManagement(
            config.getJetstream().toJetStreamOptions());

        // initialize the given stream configurations
        for (NatsConnectionFactoryConfig.JetStreamConfiguration.StreamConfiguration stream : config.getJetstream()
            .getStreams()) {
            if (stream.isCreateOrUpdate()) {
                createOrUpdateStream(stream, jetStreamManagement);
            }
        }
    }

    private void createOrUpdateStream(NatsConnectionFactoryConfig.JetStreamConfiguration.StreamConfiguration stream,
                                      JetStreamManagement jetStreamManagement)
        throws IOException, JetStreamApiException {
        final StreamConfiguration streamConfiguration = stream.toStreamConfiguration();
        if (jetStreamManagement.getStreamNames().contains(streamConfiguration.getName())) {
            StreamInfo streamInfo = jetStreamManagement.getStreamInfo(streamConfiguration.getName());

            for (String sub : streamInfo.getConfiguration().getSubjects()) {
                if (!streamConfiguration.getSubjects().contains(sub)) {
                    streamConfiguration.getSubjects().add(sub);
                }
            }

            if (!streamInfo.getConfiguration().equals(streamConfiguration)) {
                jetStreamManagement.updateStream(streamConfiguration);
            }
        } else {
            jetStreamManagement.addStream(streamConfiguration);
        }
    }
}
