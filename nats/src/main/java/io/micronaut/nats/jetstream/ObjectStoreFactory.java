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
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.connect.NatsConnectionFactoryConfig;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.ObjectStore;
import io.nats.client.ObjectStoreManagement;
import io.nats.client.ObjectStoreOptions;
import io.nats.client.api.ObjectStoreConfiguration;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * ObjectStoreFactory.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Factory
@Experimental
public class ObjectStoreFactory {

    private final BeanContext beanContext;

    public ObjectStoreFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    private static ObjectStoreOptions buildObjectStoreOptions(NatsConnectionFactoryConfig config) {
        return ObjectStoreOptions.builder().jetStreamOptions(config.getJetstream().toJetStreamOptions()).build();
    }

    /**
     * @param config The nats connection configuration
     * @return The object store management
     * @throws IOException           in case of communication issue
     * @throws JetStreamApiException the request had an error related to the data
     */
    @EachBean(NatsConnectionFactoryConfig.class)
    ObjectStoreManagement objectStoreManagement(NatsConnectionFactoryConfig config) throws IOException, JetStreamApiException {
        if (config.getJetstream() != null) {
            ObjectStoreManagement objectStoreManagement = getConnectionByName(config.getName())
                .objectStoreManagement(buildObjectStoreOptions(config));

            // initialize the given object store configurations
            for (NatsConnectionFactoryConfig.JetStreamConfiguration.ObjectStoreConfiguration objectStore : config.getJetstream().getObjectstore()) {
                ObjectStoreConfiguration objectStoreConfiguration = objectStore.toObjectStoreConfiguration();

                if (!objectStoreManagement.getBucketNames().contains(objectStoreConfiguration.getBucketName())) {
                    objectStoreManagement.create(objectStoreConfiguration);
                }
            }
            return objectStoreManagement;
        }
        return null;
    }

    /**
     * @param injectionPoint injection point
     * @return The object store
     * @throws IOException in case of communication issue
     */
    @Singleton
    ObjectStore objectStore(@Nullable InjectionPoint<?> injectionPoint) throws IOException {
        if (injectionPoint == null) {
            return null;
        }

        AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();

        if (annotationMetadata.hasAnnotation(io.micronaut.nats.jetstream.annotation.ObjectStore.class)) {
            String bucketName = annotationMetadata.getAnnotation(io.micronaut.nats.jetstream.annotation.ObjectStore.class)
                .getRequiredValue(String.class);
            String connectionName = annotationMetadata.stringValue(NatsConnection.class, "connection")
                .orElse(NatsConnection.DEFAULT_CONNECTION);

            NatsConnectionFactoryConfig natsConnectionFactoryConfig = beanContext
                .getBean(NatsConnectionFactoryConfig.class, Qualifiers.byName(connectionName));

            // Initialize object store management before accessing the object store
            beanContext.getBean(ObjectStoreManagement.class, Qualifiers.byName(connectionName));

            ObjectStoreOptions objectStoreOptions = buildObjectStoreOptions(natsConnectionFactoryConfig);
            return getConnectionByName(connectionName).objectStore(bucketName, objectStoreOptions);
        }
        return null;
    }

    private Connection getConnectionByName(String connectionName) {
        return beanContext.findBean(Connection.class, Qualifiers.byName(connectionName))
            .orElseThrow(() -> new IllegalStateException(
                "No nats connection found for " + connectionName));
    }
}
