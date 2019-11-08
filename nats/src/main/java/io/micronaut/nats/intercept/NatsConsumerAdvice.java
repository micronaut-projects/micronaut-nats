/*
 * Copyright 2017-2019 original authors
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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.bind.NatsBinderRegistry;
import io.micronaut.nats.exception.NatsListenerException;
import io.micronaut.nats.exception.NatsListenerExceptionHandler;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.exceptions.MessageListenerException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;

/**
 * An {@link ExecutableMethodProcessor} that will process all beans annotated with {@link NatsListener}.
 * It creates and subscribes the relevant methods as consumers to Nats subjects.
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsConsumerAdvice implements ExecutableMethodProcessor<NatsListener>, AutoCloseable {

    private final BeanContext beanContext;

    private final NatsBinderRegistry binderRegistry;

    private final NatsMessageSerDesRegistry serDesRegistry;

    private final NatsListenerExceptionHandler exceptionHandler;

    private final Map<Dispatcher, ConsumerState> consumerDispatchers = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     *
     * @param beanContext The bean context
     * @param binderRegistry The registry to bind arguments to the method
     * @param serDesRegistry The serialization/deserialization registry
     * @param exceptionHandler The exception handler to use if the consumer isn't a handler
     */
    public NatsConsumerAdvice(BeanContext beanContext, NatsBinderRegistry binderRegistry,
            NatsMessageSerDesRegistry serDesRegistry, NatsListenerExceptionHandler exceptionHandler) {
        this.beanContext = beanContext;
        this.binderRegistry = binderRegistry;
        this.serDesRegistry = serDesRegistry;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<Subject> subjectAnn = method.getAnnotation(Subject.class);

        if (subjectAnn != null) {
            String subject = subjectAnn.getRequiredValue(String.class);

            String clientTag = method.getDeclaringType().getSimpleName() + '#' + method;

            String connectionName =
                    method.findAnnotation(NatsConnection.class).flatMap(conn -> conn.get("connection", String.class))
                            .orElse(NatsConnection.DEFAULT_CONNECTION);

            io.micronaut.context.Qualifier<Object> qualifer =
                    beanDefinition.getAnnotationTypeByStereotype(Qualifier.class)
                            .map(type -> Qualifiers.byAnnotation(beanDefinition, type)).orElse(null);

            Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();

            Class<?> returnTypeClass = method.getReturnType().getType();
            boolean isVoid = returnTypeClass == Void.class || returnTypeClass == void.class;

            Object bean = beanContext.findBean(beanType, qualifer).orElseThrow(
                    () -> new MessageListenerException("Could not find the bean to execute the method " + method));

            Connection connection = beanContext.getBean(Connection.class, Qualifiers.byName(connectionName));

            DefaultExecutableBinder<Message> binder = new DefaultExecutableBinder<>();

            Dispatcher ds = connection.createDispatcher(msg -> {
                BoundExecutable boundExecutable = null;
                try {
                    boundExecutable = binder.bind(method, binderRegistry, msg);
                } catch (Throwable e) {
                    handleException(
                            new NatsListenerException("An error occurred binding the message to the method", e, bean,
                                    msg));
                }

                if (boundExecutable != null) {
                    Object returnedValue = boundExecutable.invoke(bean);

//                    if (!isVoid && StringUtils.isNotEmpty(msg.getReplyTo())) {
//                        // Todo implement rpc
//                    }
                }
            });

            ds.subscribe(subject);

            ConsumerState state = new ConsumerState();
            state.consumerTag = clientTag;
            state.subject = subject;
            consumerDispatchers.put(ds, state);

        }
    }

    @Override
    public void close() throws Exception {
        final Iterator<Map.Entry<Dispatcher, ConsumerState>> it = consumerDispatchers.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Dispatcher, ConsumerState> entry = it.next();
            Dispatcher dispatcher = entry.getKey();
            ConsumerState state = entry.getValue();
            dispatcher.unsubscribe(state.subject);
            if (!dispatcher.isActive()) {
                it.remove();
            }
        }
    }

    private void handleException(NatsListenerException exception) {
        Object bean = exception.getListener();
        if (bean instanceof NatsListenerExceptionHandler) {
            ((NatsListenerExceptionHandler) bean).handle(exception);
        } else {
            exceptionHandler.handle(exception);
        }
    }

    /**
     * Consumer state.
     */
    private static class ConsumerState {
        private String consumerTag;
        private String subject;
    }
}
