/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.jetstream.exception;

import java.util.Optional;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.messaging.exceptions.MessageListenerException;
import io.nats.client.Message;

/**
 * Exception thrown when an error occurs processing a Nats message via a
 * {@link io.micronaut.nats.jetstream.annotation.JetStreamListener}.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
public class JetStreamListenerException extends MessageListenerException {

    private final Object listener;

    private final Message messageState;

    /**
     * Creates a new exception.
     *
     * @param message      The message
     * @param listener     The listener
     * @param messageState The message
     */
    public JetStreamListenerException(String message, Object listener,
        @Nullable Message messageState) {
        super(message);
        this.listener = listener;
        this.messageState = messageState;
    }

    /**
     * Creates a new exception.
     *
     * @param message      The message
     * @param cause        The cause
     * @param listener     The listener
     * @param messageState The message
     */
    public JetStreamListenerException(String message, Throwable cause, Object listener,
        @Nullable Message messageState) {
        super(message, cause);
        this.listener = listener;
        this.messageState = messageState;
    }

    /**
     * Creates a new exception.
     *
     * @param message  The message
     * @param cause    The cause
     * @param listener The listener
     */
    public JetStreamListenerException(String message, Throwable cause, Object listener) {
        super(message, cause);
        this.listener = listener;
        this.messageState = null;
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
