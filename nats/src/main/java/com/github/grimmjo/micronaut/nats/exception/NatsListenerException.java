package com.github.grimmjo.micronaut.nats.exception;

import java.util.Optional;

import javax.annotation.Nullable;

import io.micronaut.messaging.exceptions.MessageListenerException;
import io.nats.client.Message;

/**
 * Exception thrown when an error occurs processing a Nats message via a {@link com.github.grimmjo.micronaut.nats.annotation.NatsListener}
 *
 * @author jgrimm
 * @since 1.0.0
 */
public class NatsListenerException extends MessageListenerException {

    private final Object listener;

    private final Message messageState;


    /**
     * Creates a new exception.
     *
     * @param message The message
     * @param cause The cause
     * @param listener The listener
     * @param messageState The message
     */
    public NatsListenerException(String message, Throwable cause, Object listener, @Nullable Message messageState) {
        super(message, cause);
        this.listener = listener;
        this.messageState = messageState;
    }

    /**
     * @return The bean that is the message listener
     */
    public Object getListener() {
        return listener;
    }

    /**
     * @return The message that produced the error
     */
    public Optional<Message> getMessageState() {
        return Optional.ofNullable(messageState);
    }
}
