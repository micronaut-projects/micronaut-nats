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
package io.micronaut.nats.jetstream.intercept;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.exceptions.MessageListenerException;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.bind.NatsBinderRegistry;
import io.micronaut.nats.jetstream.annotation.JetStreamListener;
import io.micronaut.nats.jetstream.annotation.Stream;
import io.micronaut.nats.jetstream.exception.JetStreamListenerException;
import io.micronaut.nats.jetstream.exception.JetStreamListenerExceptionHandler;
import io.micronaut.runtime.ApplicationConfiguration;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.Subscription;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

/**
 * An {@link ExecutableMethodProcessor} that will process all
 * beans annotated with
 * {@link JetStreamListener}.
 * It creates and subscribes the relevant methods as consumers to Jetstream streams.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Singleton
@Bean(preDestroy = "close")
public class JetStreamConsumerAdvice implements ExecutableMethodProcessor<Stream>, AutoCloseable {

    private final BeanContext beanContext;

    private final NatsBinderRegistry binderRegistry;

    private final JetStreamListenerExceptionHandler exceptionHandler;

    private final ApplicationConfiguration applicationConfiguration;

    private final Map<String, ConsumerState> consumers = new ConcurrentHashMap<>();

    private final AtomicInteger clientIdGenerator = new AtomicInteger(10);

    /**
     * Default constructor.
     *
     * @param beanContext              The bean context
     * @param binderRegistry           The registry to bind arguments to the method
     * @param exceptionHandler         The exception handler to use if the consumer isn't a handler
     * @param applicationConfiguration The application configuration
     */
    public JetStreamConsumerAdvice(BeanContext beanContext, NatsBinderRegistry binderRegistry,
        JetStreamListenerExceptionHandler exceptionHandler,
        ApplicationConfiguration applicationConfiguration) {
        this.beanContext = beanContext;
        this.binderRegistry = binderRegistry;
        this.exceptionHandler = exceptionHandler;
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        final Optional<AnnotationValue<JetStreamListener>> listenerAnnotation =
            method.findAnnotation(JetStreamListener.class);
        final Optional<AnnotationValue<Stream>> streamAnnotation =
            method.findAnnotation(Stream.class);
        final Optional<AnnotationValue<Subject>> subjectAnnotation =
            method.findAnnotation(Subject.class);
        final Optional<AnnotationValue<io.micronaut.nats.jetstream.annotation.ConsumerConfiguration>>
            consumerConfigurationAnnotationValue = method.findAnnotation(
            io.micronaut.nats.jetstream.annotation.ConsumerConfiguration.class);

        if (!listenerAnnotation.isPresent()) {
            // Nothing to do if no consumer or subject annotation is present
            return;
        }

        if (streamAnnotation.isPresent() && !subjectAnnotation.isPresent()) {
            throw new MessageListenerException(
                "The @Subject Annotation is missing for the method " + method);
        }

        String durable = consumerConfigurationAnnotationValue.flatMap(a -> a.getValue(String.class))
                                                             .filter(StringUtils::isNotEmpty)
                                                             .orElseThrow(
                                                                 () -> new MessageListenerException(
                                                                     "In the @ConsumerConfiguration Annotation is the value attribute "
                                                                         + "missing for the method "
                                                                         + method));

        String subject = subjectAnnotation.flatMap(a -> a.getValue(String.class))
                                          .filter(StringUtils::isNotEmpty)
                                          .orElseThrow(() -> new MessageListenerException(
                                              "In the @Subject Annotation is the value attribute "
                                                  + "missing for the " + "method " + method));
        String streamName = streamAnnotation.flatMap(a -> a.getValue(String.class))
                                            .filter(StringUtils::isNotEmpty)
                                            .orElseThrow(() -> new MessageListenerException(
                                                "In the @Stream Annotation is the value attribute"
                                                    + " missing for the method " + method));

        String connectionName = method.stringValue(NatsConnection.class, "connection")
                                      .orElse(NatsConnection.DEFAULT_CONNECTION);

        Qualifier<Object> qualifier =
            beanDefinition.getAnnotationTypeByStereotype("javax.inject.Qualifier")
                          .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                          .orElse(null);

        Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();

        Object bean = beanContext.findBean(beanType, qualifier)
                                 .orElseThrow(() -> new MessageListenerException(
                                     "Could not find the bean to execute the method " + method));

        final String clientId = listenerAnnotation.flatMap(a -> a.stringValue("clientId"))
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

        JetStream jetStream =
            beanContext.getBean(JetStream.class, Qualifiers.byName(connectionName));

        //TODO @Requires is not working
        beanContext.getBean(JetStreamManagement.class, Qualifiers.byName(connectionName));

        Connection connection =
            beanContext.getBean(Connection.class, Qualifiers.byName(connectionName));

        DefaultExecutableBinder<Message> binder = new DefaultExecutableBinder<>();

        Dispatcher ds = connection.createDispatcher();

        MessageHandler messageHandler = msg -> {
            try {
                BoundExecutable boundExecutable = binder.bind(method, binderRegistry, msg);
                if (boundExecutable != null) {
                    boundExecutable.invoke(bean);
                }
            } catch (Throwable e) {
                handleException(new JetStreamListenerException(
                    "An error occurred binding the message to the method", e, bean, msg));
            }

        };

        Set<Subscription> subscriptions = new HashSet<>();
        try {
            PushSubscribeOptions options =
                buildConsumerConfiguration(streamName, durable,
                    consumerConfigurationAnnotationValue.get());

            subscriptions.add(jetStream.subscribe(subject, ds, messageHandler, true, options));

            //TODO handle autoAck

        } catch (IOException | JetStreamApiException e) {
            handleException(new JetStreamListenerException(
                "An error occurred binding the message to the method", e, bean));
        }

        consumers.put(clientId, new ConsumerState(clientId, subscriptions, ds, connection));
    }

    private PushSubscribeOptions buildConsumerConfiguration(String streamName, String durable,
        @NonNull AnnotationValue<io.micronaut.nats.jetstream.annotation.ConsumerConfiguration> annotationValue) {

        ConsumerConfiguration.Builder builder = ConsumerConfiguration.builder().durable(durable);

        final Optional<DeliverPolicy> deliverPolicy =
            annotationValue.enumValue("deliverPolicy", DeliverPolicy.class);
        if (deliverPolicy.isPresent()) {
            builder = builder.deliverPolicy(deliverPolicy.get());
        }

        final Optional<String> deliverSubject =
            annotationValue.stringValue("deliverSubject").filter(StringUtils::isNotEmpty);
        if (deliverSubject.isPresent()) {
            builder = builder.deliverSubject(deliverSubject.get());
        }

        final OptionalLong startSequence = annotationValue.longValue("startSequence");
        if (startSequence.isPresent()) {
            builder.startSequence(startSequence.getAsLong());
        }

        final Optional<AckPolicy> ackPolicy =
            annotationValue.enumValue("ackPolicy", AckPolicy.class);
        if (ackPolicy.isPresent()) {
            builder = builder.ackPolicy(ackPolicy.get());
        }

        final OptionalLong ackWait = annotationValue.longValue("ackWait");
        if (ackWait.isPresent()) {
            builder = builder.ackWait(ackWait.getAsLong());
        }

        final Optional<ReplayPolicy> replayPolicy =
            annotationValue.enumValue("replayPolicy", ReplayPolicy.class);
        if (replayPolicy.isPresent()) {
            builder = builder.replayPolicy(replayPolicy.get());
        }

        final OptionalLong maxDeliver = annotationValue.longValue("maxDeliver");
        if (maxDeliver.isPresent()) {
            builder = builder.maxDeliver(maxDeliver.getAsLong());
        }

        final Optional<String> filterSubject =
            annotationValue.stringValue("filterSubject").filter(StringUtils::isNotEmpty);
        if (filterSubject.isPresent()) {
            builder = builder.filterSubject(filterSubject.get());
        }

        final OptionalLong rateLimit = annotationValue.longValue("rateLimit");
        if (rateLimit.isPresent()) {
            builder = builder.rateLimit(rateLimit.getAsLong());
        }

        final Optional<String> sampleFrequency =
            annotationValue.stringValue("sampleFrequency").filter(StringUtils::isNotEmpty);
        if (sampleFrequency.isPresent()) {
            builder = builder.sampleFrequency(sampleFrequency.get());
        }

        final OptionalLong idleHeartbeat = annotationValue.longValue("idleHeartbeat");
        if (idleHeartbeat.isPresent()) {
            builder = builder.idleHeartbeat(idleHeartbeat.getAsLong());
        }

        final OptionalLong flowControl = annotationValue.longValue("flowControl");
        if (flowControl.isPresent()) {
            builder = builder.flowControl(flowControl.getAsLong());
        }

        final long[] backoff = annotationValue.longValues("backoff");
        if (backoff.length > 0) {
            builder = builder.backoff(backoff);
        }

        final Optional<Boolean> headersOnly = annotationValue.booleanValue("headersOnly");
        if (headersOnly.isPresent()) {
            builder = builder.headersOnly(headersOnly.get());
        }

        final OptionalLong maxBatch = annotationValue.longValue("maxBatch");
        if (maxBatch.isPresent()) {
            builder = builder.maxBatch(maxBatch.getAsLong());
        }

        final OptionalLong maxBytes = annotationValue.longValue("maxBytes");
        if (maxBytes.isPresent()) {
            builder = builder.maxBytes(maxBytes.getAsLong());
        }

        final OptionalLong maxAckPending = annotationValue.longValue("maxAckPending");
        if (maxAckPending.isPresent()) {
            builder = builder.maxAckPending(maxAckPending.getAsLong());
        }

        final OptionalLong maxExpires = annotationValue.longValue("maxExpires");
        if (maxExpires.isPresent()) {
            builder = builder.maxExpires(maxExpires.getAsLong());
        }

        final Optional<String> deliverGroup =
            annotationValue.stringValue("deliverGroup").filter(StringUtils::isNotEmpty);
        if (deliverGroup.isPresent()) {
            builder = builder.deliverGroup(deliverGroup.get());
        }

        final Optional<String> description =
            annotationValue.stringValue("description").filter(StringUtils::isNotEmpty);
        if (description.isPresent()) {
            builder = builder.description(description.get());
        }

        final OptionalLong inactiveThreshold = annotationValue.longValue("inactiveThreshold");
        if (inactiveThreshold.isPresent()) {
            builder = builder.inactiveThreshold(inactiveThreshold.getAsLong());
        }

        return PushSubscribeOptions.builder()
                                   .stream(streamName)
                                   .durable(durable)
                                   .configuration(builder.build())
                                   .build();
    }

    @PreDestroy
    @Override
    public void close() {
        for (ConsumerState consumerState : consumers.values()) {
            if (consumerState.connection.getStatus() != Connection.Status.CLOSED) {
                consumerState.connection.closeDispatcher(consumerState.dispatcher);
            }
        }
        consumers.clear();
    }

    private void handleException(JetStreamListenerException exception) {
        Object bean = exception.getListener();
        if (bean instanceof JetStreamListenerExceptionHandler) {
            ((JetStreamListenerExceptionHandler) bean).handle(exception);
        } else {
            exceptionHandler.handle(exception);
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
