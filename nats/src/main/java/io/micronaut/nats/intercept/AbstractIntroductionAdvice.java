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
package io.micronaut.nats.intercept;


import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
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
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Abstract Implementation for nats introduction advices.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
public abstract class AbstractIntroductionAdvice {

    protected final BeanContext beanContext;

    protected final Scheduler scheduler;

    protected final NatsMessageSerDesRegistry serDesRegistry;

    protected final ConversionService conversionService;

    /**
     * Default constructor.
     *
     * @param beanContext       The bean context
     * @param executorService   The executor to execute reactive operations on
     * @param conversionService The conversion service
     * @param serDesRegistry    The serdes registry
     */
    protected AbstractIntroductionAdvice(BeanContext beanContext,
                                         @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService,
                                         ConversionService conversionService, NatsMessageSerDesRegistry serDesRegistry) {
        this.beanContext = beanContext;
        this.scheduler = Schedulers.fromExecutorService(executorService);
        this.conversionService = conversionService;
        this.serDesRegistry = serDesRegistry;
    }

    protected static Object handleResult(InterceptedMethod interceptedMethod, Mono<?> reactive) {
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
    }

    /**
     * build the nats message to send.
     *
     * @param context         the method invocation context
     * @param headers         the nats headers from the method
     * @param bodyArgument    the body argument
     * @param serDes          the nats message serdes
     * @param subjectOptional the optional subject
     * @return the nats message
     */
    protected Message buildNatsMessage(MethodInvocationContext<Object, Object> context,
                                       Headers headers,
                                       Argument<?> bodyArgument, NatsMessageSerDes<Object> serDes,
                                       Optional<String> subjectOptional) {
        NatsMessage.Builder builder = NatsMessage.builder();
        Argument<?>[] arguments = context.getArguments();
        Map<String, Object> parameterValues = context.getParameterValueMap();
        for (Argument<?> argument : arguments) {
            AnnotationValue<MessageHeader> headerAnn = argument.getAnnotation(MessageHeader.class);
            boolean headersObject = argument.getType() == Headers.class;
            if (headerAnn != null) {
                Map.Entry<String, List<String>> entry =
                    getNameAndValue(argument, headerAnn, parameterValues);
                String name = entry.getKey();
                List<String> value = entry.getValue();
                if (value.isEmpty() && headers.containsKey(name)) {
                    headers.remove(name);
                } else {
                    headers.put(name, value);
                }
            } else if (headersObject) {
                Headers dynamicHeaders = (Headers) parameterValues.get(argument.getName());
                dynamicHeaders.forEach(headers::put);
            }

        }

        if (!headers.isEmpty()) {
            builder.headers(headers);
        }

        Object body = parameterValues.get(bodyArgument.getName());
        byte[] converted = serDes.serialize(body);
        builder = builder.data(converted);

        String subject = subjectOptional.orElse(findSubjectKey(context).orElse(null));
        builder = builder.subject(subject);
        if (subject == null) {
            throw new IllegalStateException(
                "No @Subject annotation present on method: " + context.getExecutableMethod());
        }

        return builder.build();
    }

    /**
     * builds the cacheable publisher state.
     *
     * @param method the executable method
     * @return the publisher state
     */
    protected StaticPublisherState buildPublisherState(ExecutableMethod<?, ?> method) {
        Optional<String> subject =
            method.findAnnotation(Subject.class).flatMap(AnnotationValue::stringValue);

        String connection = method.stringValue(NatsConnection.class, "connection")
            .orElse(NatsConnection.DEFAULT_CONNECTION);

        Argument<?> bodyArgument = findBodyArgument(method).orElseThrow(
            () -> new NatsClientException(
                "No valid message body argument found for method: " + method));

        Headers methodHeaders = new Headers();

        List<AnnotationValue<MessageHeader>> headerAnnotations =
            method.getAnnotationValuesByType(MessageHeader.class);
        Collections.reverse(headerAnnotations); //set the values in the class first
        // so methods can override
        headerAnnotations.forEach(header -> {
            String name = header.stringValue("name").orElse(null);
            String value = header.stringValue().orElse(null);

            if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                methodHeaders.put(name, value);
            }
        });

        NatsMessageSerDes<?> serDes =
            serDesRegistry.findSerdes(bodyArgument).orElseThrow(
                () -> new NatsClientException(
                    String.format(
                        "Could not find a serializer for the body argument of type "
                            + "[%s]",
                        bodyArgument.getType().getName())));

        ReactivePublisher reactivePublisher;
        try {
            reactivePublisher = beanContext.getBean(ReactivePublisher.class,
                Qualifiers.byName(connection));
        } catch (Exception e) {
            throw new NatsClientException(
                String.format(
                    "Failed to retrieve a publisher named [%s] to publish messages",
                    connection),
                e);
        }

        return new StaticPublisherState(subject.orElse(null), bodyArgument,
            methodHeaders, method.getReturnType(), connection, serDes, reactivePublisher);
    }

    private Optional<String> findSubjectKey(MethodInvocationContext<Object, Object> method) {
        Map<String, Object> argumentValues = method.getParameterValueMap();
        return Arrays.stream(method.getArguments())
            .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(Subject.class))
            .map(Argument::getName)
            .map(argumentValues::get)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .findFirst();
    }

    private Map.Entry<String, List<String>> getNameAndValue(Argument<?> argument,
                                                            AnnotationValue<?> annotationValue,
                                                            Map<String, Object> parameterValues) {
        String argumentName = argument.getName();
        String name = annotationValue.get("name", String.class)
            .filter(StringUtils::isNotEmpty)
            .orElse(annotationValue.stringValue().orElse(argumentName));
        Optional<List> value =
            conversionService.convert(parameterValues.get(argumentName),
                Argument.of(List.class, String.class));

        return new AbstractMap.SimpleEntry<>(name,
            value.orElse(Collections.emptyList()));
    }

    private Optional<Argument<?>> findBodyArgument(ExecutableMethod<?, ?> method) {
        return Optional.ofNullable(Arrays.stream(method.getArguments())
            .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(
                MessageBody.class)).findFirst().orElseGet(
                () -> Arrays.stream(method.getArguments())
                    .filter(
                        arg -> !arg.getAnnotationMetadata().hasStereotype(Subject.class))
                    .findFirst()
                    .orElse(null)));
    }

}
