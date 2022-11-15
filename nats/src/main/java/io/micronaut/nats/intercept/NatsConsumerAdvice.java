/*
 * Copyright 2017-2020 original authors
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

import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.exceptions.MessageListenerException;
import io.micronaut.nats.ConsumerRegistry;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.bind.NatsBinderRegistry;
import io.micronaut.nats.connect.SingleNatsConnectionFactoryConfig;
import io.micronaut.nats.exception.NatsListenerException;
import io.micronaut.nats.exception.NatsListenerExceptionHandler;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.runtime.ApplicationConfiguration;
import io.nats.client.Connection;
import io.nats.client.Consumer;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.Subscription;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link ExecutableMethodProcessor} that will process all beans annotated with
 * {@link NatsListener}.
 * It creates and subscribes the relevant methods as consumers to Nats subjects.
 *
 * @author Joachim Grimm
 * @since 1.0.0
 */
@Singleton
public class NatsConsumerAdvice implements ExecutableMethodProcessor<Subject>, AutoCloseable,
    ConsumerRegistry {

    private final BeanContext beanContext;

    private final NatsBinderRegistry binderRegistry;

    private final NatsMessageSerDesRegistry serDesRegistry;

    private final NatsListenerExceptionHandler exceptionHandler;

    private final ApplicationConfiguration applicationConfiguration;

    private final Map<String, ConsumerState> consumers = new ConcurrentHashMap<>();

    private final AtomicInteger clientIdGenerator = new AtomicInteger(10);

    /**
     * Default constructor.
     *
     * @param beanContext              The bean context
     * @param binderRegistry           The registry to bind arguments to the method
     * @param serDesRegistry           The serialization/deserialization registry
     * @param exceptionHandler         The exception handler to use if the consumer isn't a handler
     * @param applicationConfiguration The application configuration
     */
    public NatsConsumerAdvice(BeanContext beanContext, NatsBinderRegistry binderRegistry,
        NatsMessageSerDesRegistry serDesRegistry, NatsListenerExceptionHandler exceptionHandler,
        ApplicationConfiguration applicationConfiguration) {
        this.beanContext = beanContext;
        this.binderRegistry = binderRegistry;
        this.serDesRegistry = serDesRegistry;
        this.exceptionHandler = exceptionHandler;
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<NatsListener> listenerAnnotation = method.getAnnotation(NatsListener.class);
        List<AnnotationValue<Subject>> subjectAnnotations =
            method.getDeclaredAnnotationValuesByType(Subject.class);

        if (listenerAnnotation == null || subjectAnnotations.isEmpty()) {
            // Nothing to do if no consumer or subject annotation is present
            return;
        }

        String connectionName =
            method.stringValue(NatsConnection.class, "connection")
                  .orElse(NatsConnection.DEFAULT_CONNECTION);

        io.micronaut.context.Qualifier<Object> qualifier =
            beanDefinition.getAnnotationTypeByStereotype("javax.inject.Qualifier")
                          .map(type -> Qualifiers.byAnnotation(beanDefinition, type)).orElse(null);

        Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();

        Class<?> returnTypeClass = method.getReturnType().getType();
        boolean isVoid = returnTypeClass == Void.class || returnTypeClass == void.class;

        Object bean = beanContext.findBean(beanType, qualifier).orElseThrow(
            () -> new MessageListenerException(
                "Could not find the bean to execute the method " + method));

        Connection connection =
            beanContext.getBean(Connection.class, Qualifiers.byName(connectionName));

        DefaultExecutableBinder<Message> binder = new DefaultExecutableBinder<>();

        Dispatcher ds = connection.createDispatcher();

        final String clientId = listenerAnnotation.stringValue("clientId")
                                                  .filter(StringUtils::isNotEmpty)
                                                  .orElseGet(
                                                      () -> applicationConfiguration.getName()
                                                                                    .map(
                                                                                        s -> s + '-'
                                                                                            + NameUtils.hyphenate(
                                                                                            beanType.getSimpleName()))
                                                                                    .orElse(
                                                                                        "nats-consumer-"
                                                                                            + clientIdGenerator.incrementAndGet()));

        MessageHandler messageHandler = msg -> {
            BoundExecutable boundExecutable = null;
            try {
                boundExecutable = binder.bind(method, binderRegistry, msg);
            } catch (Throwable e) {
                handleException(
                    new NatsListenerException(
                        "An error occurred binding the message to the method",
                        e, bean,
                        msg));
            }

            if (boundExecutable != null) {
                Object returnedValue = boundExecutable.invoke(bean);
                if (!isVoid && StringUtils.isNotEmpty(msg.getReplyTo())) {
                    byte[] converted = null;
                    if (returnedValue != null) {
                        NatsMessageSerDes<Object> serDes =
                            serDesRegistry.findSerdes(method.getReturnType().asArgument())
                                          .map(NatsMessageSerDes.class::cast)
                                          .orElseThrow(() -> new NatsListenerException(
                                              String.format(
                                                  "Could not find a serializer for the "
                                                      + "body "
                                                      + "argument of type [%s]",
                                                  returnedValue.getClass().getName()), bean,
                                              msg));
                        converted = serDes.serialize(returnedValue);
                    }
                    connection.publish(msg.getReplyTo(), converted);
                }
            }
        };
        Set<Subscription> subscriptions = new HashSet<>();
        for (AnnotationValue<Subject> subjectAnnotation : subjectAnnotations) {
            String subject = subjectAnnotation.getRequiredValue(String.class);
            Optional<String> queueOptional = subjectAnnotation.get("queue", String.class);
            if (queueOptional.isPresent() && !queueOptional.get().isEmpty()) {
                subscriptions.add(ds.subscribe(subject, queueOptional.get(), messageHandler));
            } else {
                subscriptions.add(ds.subscribe(subject, messageHandler));
            }
        }

        consumers.put(clientId, new ConsumerState(clientId, subscriptions, ds, connection));
    }

    @PreDestroy
    @Override
    public void close() {
        for (ConsumerState consumerState : consumers.values()) {
            consumerState.connection.closeDispatcher(consumerState.dispatcher);
        }
        consumers.clear();
    }

    private void handleException(NatsListenerException exception) {
        if (exception.getListener() instanceof NatsListenerExceptionHandler bean) {
            bean.handle(exception);
        } else {
            exceptionHandler.handle(exception);
        }
    }

    @NonNull
    @Override
    public Consumer getConsumer(@NonNull String id) {
        ArgumentUtils.requireNonNull("id", id);
        Dispatcher dispatcher = getConsumerState(id).dispatcher;
        if (dispatcher == null) {
            throw new IllegalArgumentException("No consumer found for ID:" + id);
        }
        return dispatcher;
    }

    @NonNull
    @Override
    public Set<String> getConsumerIds() {
        return Collections.unmodifiableSet(consumers.keySet());
    }

    @NonNull
    private ConsumerState getConsumerState(@NonNull String id) {
        ConsumerState consumerState = consumers.get(id);
        if (consumerState == null) {
            throw new IllegalArgumentException("No consumer found for ID: " + id);
        }
        return consumerState;
    }

    @NonNull
    @Override
    public Set<Subscription> getConsumerSubscription(@NonNull final String id) {
        ArgumentUtils.requireNonNull("id", id);
        final Set<Subscription> subscriptions = getConsumerState(id).subscriptions;
        if (subscriptions == null || subscriptions.isEmpty()) {
            throw new IllegalArgumentException("No consumer subscription found for ID: " + id);
        }
        return subscriptions;
    }

    @Override
    public Subscription newSubscription(@NonNull String subject, @Nullable String queue) {
        Connection connection =
            beanContext.getBean(Connection.class, Qualifiers.byName(
                SingleNatsConnectionFactoryConfig.DEFAULT_NAME));
        if (queue == null) {
            return connection.subscribe(subject);
        } else {
            return connection.subscribe(subject, queue);
        }
    }

    @Override
    public Subscription newSubscription(@NonNull String connectionName, @NonNull String subject,
        @Nullable String queue) {
        Connection connection =
            beanContext.getBean(Connection.class, Qualifiers.byName(connectionName));
        if (queue == null) {
            return connection.subscribe(subject);
        } else {
            return connection.subscribe(subject, queue);
        }
    }

    /**
     * The internal state of the consumer.
     *
     * @author Joachim Grimm
     */
    private static final class ConsumerState {

        final String clientId;

        final Set<Subscription> subscriptions;

        final Dispatcher dispatcher;

        final Connection connection;

        private ConsumerState(String clientId, Set<Subscription> subscriptions,
            Dispatcher dispatcher, Connection connection) {
            this.clientId = clientId;
            this.subscriptions = subscriptions;
            this.dispatcher = dispatcher;
            this.connection = connection;
        }
    }
}
