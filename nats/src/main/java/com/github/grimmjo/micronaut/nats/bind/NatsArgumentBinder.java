package com.github.grimmjo.micronaut.nats.bind;

import io.micronaut.core.bind.ArgumentBinder;
import io.nats.client.Message;

/**
 * An interface for Nats argument binding
 *
 * @param <T> The type of argument to be bound
 * @author jgrimm
 * @since 1.0.0
 */
public interface NatsArgumentBinder<T> extends ArgumentBinder<T, Message> {}
