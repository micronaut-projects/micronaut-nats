package com.github.grimmjo.micronaut.nats.serdes;

import java.util.Optional;

import io.micronaut.core.type.Argument;

/**
 * A registry of {@link NatsMessageSerDes} instances. Responsible for returning the serdes
 * that support the given type.
 *
 * @see NatsMessageSerDes#supports(Argument)
 * @author jgrimm
 * @since 1.0.0
 */
public interface NatsMessageSerDesRegistry {

    /**
     * Returns the serdes that supports the given type.
     *
     * @param type The type
     * @param <T> The type to be serialized/deserialized
     * @return An optional serdes
     */
    <T> Optional<NatsMessageSerDes<T>> findSerdes(Argument<T> type);
}
