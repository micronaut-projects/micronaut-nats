package com.github.grimmjo.micronaut.nats.serdes;

import java.util.Arrays;
import java.util.Optional;

import javax.inject.Singleton;

import io.micronaut.core.type.Argument;

/**
 * Default implementation of {@link NatsMessageSerDesRegistry}
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class DefaultNatsMessageSerDesRegistry implements NatsMessageSerDesRegistry {

    private final NatsMessageSerDes<?>[] serDes;

    /**
     * Default constructor.
     *
     * @param serDes The serdes to be registered.
     */
    public DefaultNatsMessageSerDesRegistry(NatsMessageSerDes<?>... serDes) {
        this.serDes = serDes;
    }

    @Override
    public <T> Optional<NatsMessageSerDes<T>> findSerdes(Argument<T> type) {
        return Arrays.stream(serDes)
                .filter(serDes -> serDes.supports((Argument) type))
                .map(serDes -> (NatsMessageSerDes<T>) serDes)
                .findFirst();
    }
}
