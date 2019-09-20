package com.github.grimmjo.micronaut.nats.exception;

import io.micronaut.messaging.exceptions.MessagingClientException;

/**
 * Exception thrown when an error occurs publishing a Nats message
 *
 * @author jgrimm
 * @since 1.0.0
 */
public class NatsClientException extends MessagingClientException {

    /**
     * Creates a new exception
     *
     * @param message The message
     */
    public NatsClientException(String message) {
        super(message);
    }
}
