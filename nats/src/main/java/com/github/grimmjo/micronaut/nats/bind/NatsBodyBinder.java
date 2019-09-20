package com.github.grimmjo.micronaut.nats.bind;

import java.util.Optional;

import javax.inject.Singleton;

import com.github.grimmjo.micronaut.nats.serdes.NatsMessageSerDesRegistry;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.messaging.annotation.Body;
import io.nats.client.Message;

/**
 * Binds an argument of with the {@link Body} annotation from the {@link Message}.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsBodyBinder implements NatsAnnotatedArgumentBinder<Body> {

    private final NatsMessageSerDesRegistry serDesRegistry;

    /**
     * Default constructor.
     *
     * @param serDesRegistry The registry to get a deserializer
     */
    public NatsBodyBinder(NatsMessageSerDesRegistry serDesRegistry) {this.serDesRegistry = serDesRegistry;}

    @Override
    public Class<Body> getAnnotationType() {
        return Body.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, Message messageState) {
        Argument<Object> bodyType = context.getArgument();
        Optional<Object> message = serDesRegistry.findSerdes(bodyType)
                .map(serDes -> serDes.deserialize(messageState, bodyType));

        return () -> message;
    }
}
