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
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.connect.NatsConnectionFactoryConfig;
import io.micronaut.nats.jetstream.annotation.KeyValueStore;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.KeyValue;
import io.nats.client.KeyValueManagement;
import io.nats.client.KeyValueOptions;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * KeyValueFactory.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Factory
public class KeyValueFactory {

    private final BeanContext beanContext;

    @Inject
    public KeyValueFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }


    /**
     * @param config The nats connection configuration
     * @return The keyvalue management
     * @throws IOException           in case of communication issue
     * @throws JetStreamApiException the request had an error related to the data
     */
    @Singleton
    @EachBean(NatsConnectionFactoryConfig.class)
    KeyValueManagement keyValueManagement(NatsConnectionFactoryConfig config) throws IOException, JetStreamApiException {
        if (config.getJetstream() != null) {
            KeyValueManagement keyValueManagement = getConnectionByName(config.getName()).keyValueManagement(KeyValueOptions.builder().jetStreamOptions(config.getJetstream().toJetStreamOptions()).build());

            // initialize the given keyvalue configurations
            for (NatsConnectionFactoryConfig.JetStreamConfiguration.KeyValueConfiguration keyValue : config.getJetstream().getKeyvalue()) {
                KeyValueConfiguration keyValueConfiguration = keyValue.toKeyValueConfiguration();

                if (keyValueManagement.getBucketNames().contains(keyValueConfiguration.getBucketName())) {
                    KeyValueStatus status = keyValueManagement.getStatus(keyValueConfiguration.getBucketName());
                    if (!status.getConfiguration().equals(keyValueConfiguration)) {
                        keyValueManagement.update(keyValueConfiguration);
                    }
                } else {
                    keyValueManagement.create(keyValueConfiguration);
                }
            }
            return keyValueManagement;
        }
        return null;
    }

    /**
     * @param injectionPoint injection point
     * @return The key value bucket
     * @throws IOException in case of communication issue
     */
    @Prototype
    KeyValue keyvalue(@Nullable InjectionPoint<?> injectionPoint) throws IOException, JetStreamApiException {
        if (injectionPoint == null) {
            return null;
        }

        AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();

        if (annotationMetadata.hasAnnotation(KeyValueStore.class)) {
            String bucketName = annotationMetadata.getAnnotation(KeyValueStore.class)
                .getRequiredValue(String.class);
            String connectionName = annotationMetadata.stringValue(NatsConnection.class, "connection")
                .orElse(NatsConnection.DEFAULT_CONNECTION);

            NatsConnectionFactoryConfig natsConnectionFactoryConfig = beanContext.getBean(NatsConnectionFactoryConfig.class, Qualifiers.byName(connectionName));
            // Initialize key value management before accessing the key store
            keyValueManagement(natsConnectionFactoryConfig);
            return getConnectionByName(connectionName).keyValue(bucketName,
                KeyValueOptions.builder().jetStreamOptions(natsConnectionFactoryConfig.getJetstream().toJetStreamOptions()).build());
        }
        return null;
    }

    private Connection getConnectionByName(String connectionName) {
        return beanContext.findBean(Connection.class, Qualifiers.byName(connectionName))
            .orElseThrow(() -> new IllegalStateException(
                "No nats connection found for " + connectionName));
    }
}
