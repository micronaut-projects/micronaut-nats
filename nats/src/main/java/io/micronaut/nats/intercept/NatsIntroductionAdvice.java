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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Singleton;

import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.exception.NatsClientException;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.caffeine.cache.Cache;
import io.micronaut.caffeine.cache.Caffeine;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.annotation.Body;
import io.nats.client.Connection;

/**
 * Implementation of the {@link NatsClient} advice annotation.
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsIntroductionAdvice implements MethodInterceptor<Object, Object> {

    private final BeanContext beanContext;

    private final NatsMessageSerDesRegistry serDesRegistry;

    private final Cache<ExecutableMethod, StaticPublisherState> publisherCache = Caffeine.newBuilder().build();

    /**
     * Default constructor.
     * @param beanContext    The bean context
     * @param serDesRegistry The serialization/deserialization registry
     */
    public NatsIntroductionAdvice(BeanContext beanContext, NatsMessageSerDesRegistry serDesRegistry) {
        this.beanContext = beanContext;
        this.serDesRegistry = serDesRegistry;
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

                return new StaticPublisherState(subject.orElse(null), bodyArgument, method.getReturnType(), connection,
                        serDes);
            });

            Map<String, Object> parameterValues = context.getParameterValueMap();
            Object body = parameterValues.get(publisherState.getBodyArgument().getName());
            byte[] converted = publisherState.getSerDes().serialize(body);
            String subject = publisherState.getSubject().orElse(findSubjectKey(context).orElse(null));
            if (subject == null) {
                throw new IllegalStateException(
                        "No @Subject annotation present on method: " + context.getExecutableMethod());
            }

            Connection connection =
                    beanContext.getBean(Connection.class, Qualifiers.byName(publisherState.getConnection()));
            connection.publish(subject, converted);
        }

        return null;
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
