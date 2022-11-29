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
package io.micronaut.nats.jetstream.intercept;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.nats.exception.NatsClientException;
import io.micronaut.nats.intercept.AbstractIntroductionAdvice;
import io.micronaut.nats.intercept.StaticPublisherState;
import io.micronaut.nats.jetstream.annotation.JetStreamClient;
import io.micronaut.nats.jetstream.reactive.ReactivePublisher;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.scheduling.TaskExecutors;
import io.nats.client.Message;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of the {@link JetStreamClient} advice annotation.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
@InterceptorBean(JetStreamClient.class)
public class JetStreamIntroductionAdvice extends AbstractIntroductionAdvice implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(JetStreamIntroductionAdvice.class);

    private final Map<ExecutableMethod<?, ?>, JetStreamPublisherState> publisherCache =
        new ConcurrentHashMap<>();

    /**
     * Default constructor.
     *
     * @param beanContext       The bean context
     * @param conversionService The conversion service
     * @param serDesRegistry    The serialization/deserialization registry
     * @param executorService   The executor to execute reactive operations on
     */
    public JetStreamIntroductionAdvice(BeanContext beanContext,
                                       ConversionService conversionService,
                                       NatsMessageSerDesRegistry serDesRegistry,
                                       @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService) {
        super(beanContext, executorService, conversionService, serDesRegistry);
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (context.hasAnnotation(JetStreamClient.class)) {
            JetStreamPublisherState publisherState =
                publisherCache.computeIfAbsent(context.getExecutableMethod(), method -> {
                    final StaticPublisherState staticPublisherState = buildPublisherState(method);
                    ReactivePublisher reactivePublisher;
                    try {
                        reactivePublisher = beanContext.getBean(
                            ReactivePublisher.class,
                            Qualifiers.byName(staticPublisherState.getConnection()));
                    } catch (Exception e) {
                        throw new NatsClientException(
                            String.format(
                                "Failed to retrieve a publisher named [%s] to publish messages",
                                staticPublisherState.getConnection()),
                            e);
                    }

                    final Optional<Argument<?>> publishOptions =
                        Arrays.stream(method.getArguments())
                              .filter(
                                  arg -> PublishOptions.class.isAssignableFrom(
                                      arg.getType()))
                              .findFirst();

                    return new JetStreamPublisherState(staticPublisherState, reactivePublisher,
                        publishOptions);
                });

            Message message = buildNatsMessage(context, publisherState.getHeaders(),
                publisherState.getBodyArgument(),
                publisherState.getSerDes(), publisherState.getSubject());
            ReactivePublisher reactivePublisher = publisherState.getJetstreanReactivePublisher();
            InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

            try {
                Mono<PublishAck> reactive;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending the message from context {}.", context);
                }
                final Optional<Argument<?>> publishOptionsOptional =
                    publisherState.getPublishOptions();
                if (publishOptionsOptional.isPresent()) {
                    final PublishOptions publishOptions = (PublishOptions)
                        context.getParameterValueMap().get(publishOptionsOptional.get().getName());
                    if (publishOptions != null) {
                        reactive = Mono.from(reactivePublisher.publish(message, publishOptions));
                    } else {
                        reactive = Mono.from(reactivePublisher.publish(message));
                    }
                } else {
                    reactive = Mono.from(reactivePublisher.publish(message));
                }

                reactive = reactive.onErrorMap(throwable -> new NatsClientException(
                    String.format(
                        "Failed to publish a message with subject: [%s]", message.getSubject()),
                    throwable, Collections.singletonList(message)));

                if (interceptedMethod.resultType() != InterceptedMethod.ResultType.SYNCHRONOUS) {
                    reactive = reactive.subscribeOn(scheduler);
                }

                return handleResult(interceptedMethod, reactive);
            } catch (Exception e) {
                return interceptedMethod.handleException(e);
            }
        } else {
            return context.proceed();
        }
    }

}
