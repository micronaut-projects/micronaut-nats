package com.github.grimmjo.micronaut.nats.bind;

import javax.inject.Singleton;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.nats.client.Message;

/**
 * The default binder for binding an argument from the {@link Message}
 * that is used if no other binder supports the argument.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsDefaultBinder implements NatsArgumentBinder<Object> {

    private final NatsBodyBinder bodyBinder;

    public NatsDefaultBinder(NatsBodyBinder bodyBinder) {this.bodyBinder = bodyBinder;}

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, Message messageState) {
        return bodyBinder.bind(context, messageState);
    }
}
