/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.intercept;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.caffeine.cache.Cache;
import io.micronaut.caffeine.cache.Caffeine;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.exception.NatsClientException;
import io.micronaut.nats.reactive.PublishState;
import io.micronaut.nats.reactive.ReactivePublisher;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.scheduling.TaskExecutors;
import io.nats.client.Message;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link NatsClient} advice annotation.
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsIntroductionAdvice implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(NatsIntroductionAdvice.class);

    private final BeanContext beanContext;

    private final Scheduler scheduler;

    private final ConversionService<?> conversionService;

    private final NatsMessageSerDesRegistry serDesRegistry;

    private final Cache<ExecutableMethod, StaticPublisherState> publisherCache = Caffeine.newBuilder().build();

    /**
     * Default constructor.
     * @param beanContext       The bean context
     * @param conversionService The conversion service
     * @param serDesRegistry    The serialization/deserialization registry
     * @param executorService   The executor to execute reactive operations on
     */
    public NatsIntroductionAdvice(BeanContext beanContext, ConversionService<?> conversionService,
            NatsMessageSerDesRegistry serDesRegistry, @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService) {
        this.beanContext = beanContext;
        this.conversionService = conversionService;
        this.serDesRegistry = serDesRegistry;
        this.scheduler = Schedulers.from(executorService);
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (context.hasAnnotation(NatsClient.class)) {
            StaticPublisherState publisherState = publisherCache.get(context.getExecutableMethod(), method -> {
                if (!method.findAnnotation(NatsClient.class).isPresent()) {
                    throw new IllegalStateException("No @NatsClient annotation present on method: " + method);
                }
                Optional<AnnotationValue<Subject>> subjectAnn = method.findAnnotation(Subject.class);
                Optional<String> subject = subjectAnn.flatMap(s -> s.getValue(String.class));

                String connection = method.findAnnotation(NatsConnection.class)
                        .flatMap(conn -> conn.get("connection", String.class))
                        .orElse(NatsConnection.DEFAULT_CONNECTION);

                Argument<?> bodyArgument = findBodyArgument(method).orElseThrow(
                        () -> new NatsClientException("No valid message body argument found for method: " + method));

                NatsMessageSerDes<?> serDes = serDesRegistry.findSerdes(bodyArgument).orElseThrow(
                        () -> new NatsClientException(
                                String.format("Could not find a serializer for the body argument of type [%s]",
                                        bodyArgument.getType().getName())));

                ReactivePublisher reactivePublisher;
                try {
                    reactivePublisher = beanContext.getBean(ReactivePublisher.class, Qualifiers.byName(connection));
                } catch (Throwable e) {
                    throw new NatsClientException(
                            String.format("Failed to retrieve a publisher named [%s] to publish messages", connection),
                            e);
                }

                return new StaticPublisherState(subject.orElse(null), bodyArgument, method.getReturnType(), connection,
                        serDes, reactivePublisher);
            });

            Map<String, Object> parameterValues = context.getParameterValueMap();
            Object body = parameterValues.get(publisherState.getBodyArgument().getName());
            byte[] converted = publisherState.getSerDes().serialize(body);
            String subject = publisherState.getSubject().orElse(findSubjectKey(context).orElse(null));
            if (subject == null) {
                throw new IllegalStateException(
                        "No @Subject annotation present on method: " + context.getExecutableMethod());
            }

            PublishState publishState = new PublishState(subject, converted);
            Class dataTypeClass = publisherState.getDataType().getType();
            boolean isVoid = dataTypeClass == void.class || dataTypeClass == Void.class;

            ReactivePublisher reactivePublisher = publisherState.getReactivePublisher();

            if (publisherState.isReactive()) {
                Publisher reactive;
                if (isVoid) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Sending the message with publisher confirms.", context);
                    }
                    reactive = Flowable.fromPublisher(reactivePublisher.publish(publishState)).subscribeOn(scheduler);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Publish is an RPC call. Publisher will complete when a response is received.",
                                context);
                    }
                    reactive = Flowable.fromPublisher(reactivePublisher.publishAndReply(publishState))
                            .flatMap(consumerState -> {
                                Object deserialized = deserialize(consumerState, publisherState.getDataType(),
                                        publisherState.getDataType());
                                if (deserialized == null) {
                                    return Flowable.empty();
                                } else {
                                    return Flowable.just(deserialized);
                                }
                            }).subscribeOn(scheduler);
                }
                return conversionService.convert(reactive, context.getReturnType().getType()).orElseThrow(
                        () -> new NatsClientException(
                                "Could not convert the publisher acknowledgement response to the return type of the "
                                        + "method", Collections.singletonList(publishState)));
            } else {
                if (isVoid) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Sending the message without publisher confirms.", context);
                    }
                    Throwable throwable =
                            Completable.fromPublisher(reactivePublisher.publish(publishState)).blockingGet();
                    if (throwable != null) {
                        throw new NatsClientException(
                                String.format("Failed to publish a message with subject: [%s]", subject), throwable,
                                Collections.singletonList(publishState));
                    }
                    return null;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Publish is an RPC call. Blocking until a response is received.", context);
                    }
                    return Single.fromPublisher(reactivePublisher.publishAndReply(publishState))
                            .flatMapMaybe(message -> {
                                Object deserialized = deserialize(message, publisherState.getDataType(),
                                        publisherState.getDataType());
                                if (deserialized == null) {
                                    return Maybe.empty();
                                } else {
                                    return Maybe.just(deserialized);
                                }
                            }).blockingGet();
                }
            }

        } else {
            return context.proceed();
        }
    }

    private Object deserialize(Message message, Argument dataType, Argument returnType) {
        Optional<NatsMessageSerDes<Object>> serDes = serDesRegistry.findSerdes(dataType);
        if (serDes.isPresent()) {
            return serDes.get().deserialize(message, returnType);
        } else {
            throw new NatsClientException(String.format("Could not find a deserializer for [%s]", dataType.getName()));
        }
    }

    private Optional<Argument<?>> findBodyArgument(ExecutableMethod<?, ?> method) {
        return Optional.ofNullable(Arrays.stream(method.getArguments())
                .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(Body.class)).findFirst().orElseGet(
                        () -> Arrays.stream(method.getArguments())
                                .filter(arg -> !arg.getAnnotationMetadata().hasStereotype(Subject.class)).findFirst()
                                .orElse(null)));
    }

    private Optional<String> findSubjectKey(MethodInvocationContext<Object, Object> method) {
        Map<String, Object> argumentValues = method.getParameterValueMap();
        return Arrays.stream(method.getArguments())
                .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(Subject.class)).map(Argument::getName)
                .map(argumentValues::get).filter(Objects::nonNull).map(Object::toString).findFirst();
    }
}
