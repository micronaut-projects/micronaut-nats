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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.caffeine.cache.Cache;
import io.micronaut.caffeine.cache.Caffeine;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.exception.NatsClientException;
import io.micronaut.nats.reactive.ReactivePublisher;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.scheduling.TaskExecutors;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Implementation of the {@link NatsClient} advice annotation.
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
@InterceptorBean(NatsClient.class)
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
        this.scheduler = Schedulers.fromExecutorService(executorService);
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

                Headers methodHeaders = new Headers();

                List<AnnotationValue<MessageHeader>> headerAnnotations =
                        method.getAnnotationValuesByType(MessageHeader.class);
                Collections.reverse(headerAnnotations); //set the values in the class first so methods can override
                headerAnnotations.forEach(header -> {
                    String name = header.get("name", String.class).orElse(null);
                    String value = header.getValue(String.class).orElse(null);

                    if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                        if (!methodHeaders.containsKey(name)) {
                            methodHeaders.put(name, new ArrayList<>());
                        }
                        methodHeaders.put(name, value);
                    }
                });

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

                return new StaticPublisherState(subject.orElse(null), bodyArgument, methodHeaders,
                        method.getReturnType(), connection,
                        serDes, reactivePublisher);
            });

            NatsMessage.Builder builder = NatsMessage.builder();
            Headers headers = publisherState.getHeaders();
            Argument[] arguments = context.getArguments();
            Map<String, Object> parameterValues = context.getParameterValueMap();
            for (Argument argument : arguments) {
                AnnotationValue<MessageHeader> headerAnn = argument.getAnnotation(MessageHeader.class);
                boolean headersObject = argument.getType() == Headers.class;
                if (headerAnn != null) {
                    Map.Entry<String, List<String>> entry = getNameAndValue(argument, headerAnn, parameterValues);
                    String name = entry.getKey();
                    List<String> value = entry.getValue();
                    headers.put(name, value);
                } else if (headersObject) {
                    Headers dynamicHeaders = (Headers) parameterValues.get(argument.getName());
                    dynamicHeaders.forEach(headers::put);
                }

            }

            if (!headers.isEmpty()) {
                builder.headers(headers);
            }

            Object body = parameterValues.get(publisherState.getBodyArgument().getName());
            byte[] converted = publisherState.getSerDes().serialize(body);
            builder = builder.data(converted);

            String subject = publisherState.getSubject().orElse(findSubjectKey(context).orElse(null));
            builder = builder.subject(subject);
            if (subject == null) {
                throw new IllegalStateException(
                        "No @Subject annotation present on method: " + context.getExecutableMethod());
            }

            Message message = builder.build();
            ReactivePublisher reactivePublisher = publisherState.getReactivePublisher();
            InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

            try {
                boolean rpc = !interceptedMethod.returnTypeValue().isVoid();

                Mono<?> reactive;
                if (rpc) {
                    reactive = Mono.from(reactivePublisher.publishAndReply(message))
                            .flatMap(response -> {
                                Object deserialized = deserialize(response, publisherState.getDataType(),
                                        publisherState.getDataType());
                                if (deserialized == null) {
                                    return Mono.empty();
                                } else {
                                    return Mono.just(deserialized);
                                }
                            });

                    if (interceptedMethod.resultType() == InterceptedMethod.ResultType.SYNCHRONOUS) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Publish is an RPC call. Blocking until a response is received.", context);
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Publish is an RPC call. Publisher will complete when a response is received.", context);
                        }
                        reactive = reactive.subscribeOn(scheduler);
                    }
                } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Sending the message.", context);
                        }
                        reactive = Mono.from(reactivePublisher.publish(message))
                                .onErrorMap(throwable -> new NatsClientException(
                                        String.format("Failed to publish a message with subject: [%s]", subject),
                                        throwable, Collections.singletonList(message)));

                }

                switch (interceptedMethod.resultType()) {
                    case PUBLISHER:
                        return interceptedMethod.handleResult(reactive);
                    case COMPLETION_STAGE:
                        CompletableFuture<Object> future = new CompletableFuture<>();
                        reactive.subscribe(new Subscriber<Object>() {
                            Object value = null;
                            @Override
                            public void onSubscribe(Subscription s) {
                                s.request(1);
                            }

                            @Override
                            public void onNext(Object o) {
                                value = o;
                            }

                            @Override
                            public void onError(Throwable t) {
                                future.completeExceptionally(t);
                            }

                            @Override
                            public void onComplete() {
                                future.complete(value);
                            }
                        });
                        return interceptedMethod.handleResult(future);
                    case SYNCHRONOUS:
                        return interceptedMethod.handleResult(reactive.block());
                    default:
                        return interceptedMethod.unsupported();
                }
            } catch (Exception e) {
                return interceptedMethod.handleException(e);
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
                                         .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(
                                                 MessageBody.class)).findFirst().orElseGet(
                        () -> Arrays.stream(method.getArguments())
                                    .filter(arg -> !arg.getAnnotationMetadata().hasStereotype(Subject.class))
                                    .findFirst()
                                    .orElse(null)));
    }

    private Optional<String> findSubjectKey(MethodInvocationContext<Object, Object> method) {
        Map<String, Object> argumentValues = method.getParameterValueMap();
        return Arrays.stream(method.getArguments())
                .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(Subject.class)).map(Argument::getName)
                .map(argumentValues::get).filter(Objects::nonNull).map(Object::toString).findFirst();
    }

    private Map.Entry<String, List<String>> getNameAndValue(Argument argument, AnnotationValue<?> annotationValue,
            Map<String, Object> parameterValues) {
        String argumentName = argument.getName();
        String name = annotationValue.get("name", String.class)
                                     .orElse(annotationValue.getValue(String.class).orElse(argumentName));
        Optional<List> value =
                conversionService.convert(parameterValues.get(argumentName), Argument.of(List.class, String.class));

        return new AbstractMap.SimpleEntry<>(name, value.orElse(Collections.emptyList()));
    }
}
