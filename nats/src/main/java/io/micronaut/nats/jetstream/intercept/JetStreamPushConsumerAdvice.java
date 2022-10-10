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
import java.util.Arrays;
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
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageListenerException;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.bind.NatsBinderRegistry;
import io.micronaut.nats.jetstream.PushConsumerRegistry;
import io.micronaut.nats.jetstream.annotation.JetStreamListener;
import io.micronaut.nats.jetstream.annotation.PushConsumer;
import io.micronaut.nats.jetstream.exception.JetStreamListenerException;
import io.micronaut.nats.jetstream.exception.JetStreamListenerExceptionHandler;
import io.micronaut.runtime.ApplicationConfiguration;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
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
public class JetStreamPushConsumerAdvice
    implements ExecutableMethodProcessor<PushConsumer>, AutoCloseable,
    PushConsumerRegistry {

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
    public JetStreamPushConsumerAdvice(BeanContext beanContext, NatsBinderRegistry binderRegistry,
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
        if (!listenerAnnotation.isPresent()) {
            // Nothing to do if no consumer or subject annotation is present
            return;
        }

        final Optional<AnnotationValue<PushConsumer>> pushConsumerAnnotation =
                method.findAnnotation(PushConsumer.class);
        final Optional<AnnotationValue<Subject>> subjectAnnotation =
                method.findAnnotation(Subject.class);

        if (!pushConsumerAnnotation.isPresent()) {
            // Ignore the current method
            return;
        }
        AnnotationValue<PushConsumer> pushConsumer = pushConsumerAnnotation.get();

        String subject = subjectAnnotation.flatMap(a -> a.getValue(String.class))
                                          .filter(StringUtils::isNotEmpty)
                                          .orElseThrow(() -> new MessageListenerException(
                                              "In the @PushConsumer Annotation is the subject"
                                                  + " attribute "
                                                  + "missing for the method " + method));
        String streamName = pushConsumer.getValue(String.class)
                                        .filter(StringUtils::isNotEmpty)
                                        .orElseThrow(() -> new MessageListenerException(
                                            "In the @PushConsumer Annotation is the "
                                                + "value attribute"
                                                + " missing for the method " + method));

        final Optional<AckPolicy> ackPolicy = pushConsumer.enumValue("ackPolicy", AckPolicy.class);

        boolean autoAck = true;
        if (ackPolicy.isPresent()) {
            AckPolicy policy = ackPolicy.get();
            if (policy != AckPolicy.All) {
                autoAck = false;
            }
            if (policy == AckPolicy.Explicit && Arrays.stream(method.getArguments())
                                                      .noneMatch(
                                                          a -> Acknowledgement.class.isAssignableFrom(
                                                              a.getType()))) {
                throw new MessageListenerException("The ackPolicy for method " + method
                    + " is explicit. The method must have a argument of type Achnowledgement");
            }
        }

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
                                                                                            "jetstream-consumer-"
                                                                                            + clientIdGenerator.incrementAndGet()));

        JetStream jetStream =
            beanContext.getBean(JetStream.class, Qualifiers.byName(connectionName));

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
            } catch (Exception e) {
                handleException(new JetStreamListenerException(
                        "An error occurred binding the message to the method", e, bean, msg));
            }

        };

        Set<Subscription> subscriptions = new HashSet<>();
        try {
            PushSubscribeOptions options = buildConsumerConfiguration(streamName, pushConsumer);

            Optional<String> queueOptional = pushConsumer.get("queue", String.class);
            if (queueOptional.isPresent() && !queueOptional.get().isEmpty()) {
                subscriptions.add(
                    jetStream.subscribe(subject, queueOptional.get(), ds, messageHandler, autoAck,
                        options));
            } else {
                subscriptions.add(
                    jetStream.subscribe(subject, ds, messageHandler, autoAck, options));
            }

        } catch (IOException | JetStreamApiException e) {
            handleException(new JetStreamListenerException(
                    "An error occurred binding the message to the method", e, bean));
        }

        consumers.put(clientId, new ConsumerState(clientId, subscriptions, ds, connection));
    }

    private PushSubscribeOptions buildConsumerConfiguration(String streamName,
            @NonNull AnnotationValue<PushConsumer> annotationValue) {

        ConsumerConfiguration.Builder builder = ConsumerConfiguration.builder();
        final Optional<String> durable = annotationValue.stringValue("durable");
        if (durable.isPresent()) {
            builder = builder.durable(durable.get());
        }

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

        ConsumerConfiguration cc = builder.build();
        return PushSubscribeOptions.builder()
                                   .stream(streamName)
                                   .durable(cc.getDurable())
                                   .configuration(cc)
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

    @Override
    public JetStreamSubscription newSubscription(String subject,
        PushSubscribeOptions pushSubscribeOptions, String queue)
        throws JetStreamApiException, IOException {
        JetStream jetStream =
            beanContext.getBean(JetStream.class,
                Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION));

        if (queue == null) {
            return jetStream.subscribe(subject, pushSubscribeOptions);
        } else {
            return jetStream.subscribe(subject, queue, pushSubscribeOptions);
        }
    }

    @Override
    public JetStreamSubscription newSubscription(String connectionName, String subject,
        PushSubscribeOptions pushSubscribeOptions, String queue)
        throws JetStreamApiException, IOException {
        JetStream jetStream =
            beanContext.getBean(JetStream.class, Qualifiers.byName(connectionName));
        if (queue == null) {
            return jetStream.subscribe(subject, pushSubscribeOptions);
        } else {
            return jetStream.subscribe(subject, queue, pushSubscribeOptions);
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
