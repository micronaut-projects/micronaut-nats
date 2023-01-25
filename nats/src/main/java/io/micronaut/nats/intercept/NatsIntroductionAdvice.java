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

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.exception.NatsClientException;
import io.micronaut.nats.reactive.ReactivePublisher;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.scheduling.TaskExecutors;
import io.nats.client.Message;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of the {@link NatsClient} advice annotation.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
@InterceptorBean(NatsClient.class)
public class NatsIntroductionAdvice extends AbstractIntroductionAdvice implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(NatsIntroductionAdvice.class);

    private final Map<ExecutableMethod, StaticPublisherState> publisherCache = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     *
     * @param beanContext       The bean context
     * @param conversionService The conversion service
     * @param serDesRegistry    The serialization/deserialization registry
     * @param executorService   The executor to execute reactive operations on
     */
    public NatsIntroductionAdvice(BeanContext beanContext,
                                  ConversionService conversionService,
                                  NatsMessageSerDesRegistry serDesRegistry,
                                  @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService) {
        super(beanContext, executorService, conversionService, serDesRegistry);
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (context.hasAnnotation(NatsClient.class)) {
            StaticPublisherState publisherState =
                publisherCache.computeIfAbsent(context.getExecutableMethod(), this::buildPublisherState);

            Message message = buildNatsMessage(context, publisherState.getHeaders(),
                publisherState.getBodyArgument(),
                publisherState.getSerDes(), publisherState.getSubject());
            ReactivePublisher reactivePublisher = publisherState.getReactivePublisher();
            InterceptedMethod interceptedMethod = InterceptedMethod.of(context, conversionService);

            try {
                boolean rpc = false;

                if (interceptedMethod.resultType() == InterceptedMethod.ResultType.SYNCHRONOUS) {
                    rpc = !interceptedMethod.returnTypeValue().isVoid();
                } else {
                    Optional<Argument<?>> firstTypeVariable = context.getReturnType().asArgument().getFirstTypeVariable();
                    if (firstTypeVariable.isPresent()) {
                        rpc = !firstTypeVariable.get().isVoid();
                    }
                }

                Mono<?> reactive;
                if (rpc) {
                    reactive = Mono.from(reactivePublisher.publishAndReply(message))
                        .flatMap(response -> {
                            Object deserialized =
                                deserialize(response, publisherState.getDataType(),
                                    publisherState.getDataType());
                            if (deserialized == null) {
                                return Mono.empty();
                            } else {
                                return Mono.just(deserialized);
                            }
                        });

                    if (interceptedMethod.resultType()
                        == InterceptedMethod.ResultType.SYNCHRONOUS) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(
                                "Publish is an RPC call. Blocking until a response is received. {}",
                                context);
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(
                                "Publish is an RPC call. Publisher will complete when a response "
                                    + "is received. {}",
                                context);
                        }
                        reactive = reactive.subscribeOn(scheduler);
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Sending the message. {}", context);
                    }
                    reactive = Mono.from(reactivePublisher.publish(message))
                        .onErrorMap(throwable -> new NatsClientException(
                            String.format(
                                "Failed to publish a message with subject: [%s]",
                                message.getSubject()),
                            throwable, Collections.singletonList(message)));

                }

                return handleResult(interceptedMethod, reactive);
            } catch (Exception e) {
                return interceptedMethod.handleException(e);
            }
        } else {
            return context.proceed();
        }
    }

    private Object deserialize(Message message, Argument<Object> dataType,
                               Argument<Object> returnType) {
        Optional<NatsMessageSerDes<Object>> serDes = serDesRegistry.findSerdes(dataType);
        if (serDes.isPresent()) {
            return serDes.get().deserialize(message, returnType);
        } else {
            throw new NatsClientException(
                String.format("Could not find a deserializer for [%s]", dataType.getName()));
        }
    }

}
