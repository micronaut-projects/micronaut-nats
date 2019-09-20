package com.github.grimmjo.micronaut.nats.intercept;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import com.github.grimmjo.micronaut.nats.annotation.NatsClient;
import com.github.grimmjo.micronaut.nats.annotation.NatsConnection;
import com.github.grimmjo.micronaut.nats.annotation.Subject;
import com.github.grimmjo.micronaut.nats.exception.NatsClientException;
import com.github.grimmjo.micronaut.nats.serdes.NatsMessageSerDes;
import com.github.grimmjo.micronaut.nats.serdes.NatsMessageSerDesRegistry;
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
 * Implementation of the {@link NatsClient} advice annotation
 *
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
     *
     * @param beanContext The bean context
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
                AnnotationValue<Subject> subjectAnn = method.findAnnotation(Subject.class).orElseThrow(
                        () -> new IllegalStateException("No @Subject annotation present on method: " + method));
                String subject = subjectAnn.getValue(String.class).get();

                String connection = method.findAnnotation(NatsConnection.class)
                        .flatMap(conn -> conn.get("connection", String.class))
                        .orElse(NatsConnection.DEFAULT_CONNECTION);

                Argument<?> bodyArgument = findBodyArgument(method).orElseThrow(
                        () -> new NatsClientException("No valid message body argument found for method: " + method));

                NatsMessageSerDes<?> serDes = serDesRegistry.findSerdes(bodyArgument).orElseThrow(
                        () -> new NatsClientException(
                                String.format("Could not find a serializer for the body argument of type [%s]",
                                        bodyArgument.getType().getName())));

                return new StaticPublisherState(subject, bodyArgument, method.getReturnType(), connection, serDes);
            });

            Map<String, Object> parameterValues = context.getParameterValueMap();
            Object body = parameterValues.get(publisherState.getBodyArgument().getName());
            byte[] converted = publisherState.getSerDes().serialize(body);

            Connection connection =
                    beanContext.getBean(Connection.class, Qualifiers.byName(publisherState.getConnection()));
            connection.publish(publisherState.getSubject(), converted);
        }

        return null;
    }

    private Optional<Argument<?>> findBodyArgument(ExecutableMethod<?, ?> method) {
        return Optional.ofNullable(Arrays.stream(method.getArguments())
                .filter(arg -> arg.getAnnotationMetadata().hasAnnotation(Body.class))
                .findFirst()
                .orElseGet(() -> Arrays.stream(method.getArguments()).findFirst().orElse(null)));
    }

}
