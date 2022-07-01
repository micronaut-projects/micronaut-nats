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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;
import io.nats.client.JetStreamOptions;

/**
 * Manages the jetstream configuration for each connection.
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Introspected
@Requires(property = JetStreamConfiguration.PREFIX)
@EachProperty(value = JetStreamConfiguration.PREFIX, primary = "default")
public class JetStreamConfiguration {

    public static final String PREFIX = "jetstream";

    private final String connectionName;

    @ConfigurationBuilder(prefixes = "")
    private JetStreamOptions.Builder builder =
        JetStreamOptions.builder(JetStreamOptions.defaultOptions());

    private Map<String, StreamConfiguration> streams = new HashMap<>();

    public JetStreamConfiguration(@Parameter String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * get the connection name for this configuration.
     *
     * @return the connection name
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * get the jetstream options builder.
     *
     * @return options builder
     */
    public JetStreamOptions.Builder getBuilder() {
        return builder;
    }

    /**
     * return the configuration as {@link JetStreamOptions}.
     *
     * @return jetstream options
     */
    public JetStreamOptions toJetStreamOptions() {
        return builder.build();
    }

    /**
     * get the stream configurations.
     *
     * @return list of streamConfigurations
     */
    public Map<String, StreamConfiguration> getStreams() {
        return streams;
    }

    /**
     * set the stream configurations for the jetstream.
     *
     * @param streams the stream configurations
     */
    public void setStreams(Map<String, StreamConfiguration> streams) {
        this.streams = streams;
    }

    /**
     * Manages a single stream configuration.
     */
    @Introspected
    @EachProperty(value = "streams")
    public static class StreamConfiguration {

        @ConfigurationBuilder(prefixes = "", excludes = { "addSubjects", "addSources",
            "name", "subjects" })
        private io.nats.client.api.StreamConfiguration.Builder builder =
            io.nats.client.api.StreamConfiguration.builder();

        private List<String> subjects;

        /**
         * get the stream configuration builder.
         *
         * @return stream configuration builder
         */
        public io.nats.client.api.StreamConfiguration.Builder getBuilder() {
            return builder;
        }

        /**
         * return the configuration as {@link StreamConfiguration}.
         *
         * @param streamName the stream name
         * @return nats stream configuration
         */
        public io.nats.client.api.StreamConfiguration toStreamConfiguration(String streamName) {
            return builder.name(streamName).subjects(subjects).build();
        }

        /**
         * get the subjects of the stream.
         *
         * @return the subjects
         */
        public List<String> getSubjects() {
            return subjects;
        }
    }
}
