package com.github.grimmjo.micronaut.nats.serdes;

import javax.annotation.Nullable;

import io.micronaut.core.order.Ordered;
import io.micronaut.core.type.Argument;
import io.nats.client.Message;

/**
 * Responsible for serializing and deserializing RabbitMQ message bodies.
 *
 * @param <T> The type to be serialized/deserialized
 * @author jgrimm
 * @since 1.0.0
 */
public interface NatsMessageSerDes<T> extends Ordered {

    /**
     * Deserializes the message into the requested type.
     *
     * @param message The message to deserialize
     * @param argument The type to be returned
     * @return The deserialized body
     */
    @Nullable
    T deserialize(Message message, Argument<T> argument);

    /**
     * Serializes the data into a byte[] to be published
     * to RabbitMQ.
     *
     * @param data The data to serialize
     * @return The message body
     */
    @Nullable
    byte[] serialize(@Nullable T data);

    /**
     * Determines if this serdes supports the given type.
     *
     * @param type The type
     * @return True if the type is supported
     */
    boolean supports(Argument<T> type);
}
