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
package io.micronaut.nats.exception;

import java.util.List;

import io.micronaut.messaging.exceptions.MessagingClientException;
import io.nats.client.Message;

/**
 * Exception thrown when an error occurs publishing a Nats message.
 * @author jgrimm
 * @since 1.0.0
 */
public class NatsClientException extends MessagingClientException {

    private final List<Message> messages;

    /**
     * Creates a new exception.
     * @param message The message
     */
    public NatsClientException(String message) {
        super(message);
        this.messages = null;
    }

    /**
     * Creates a new exception.
     * @param message  The message
     * @param messages The messages that failed to send
     */
    public NatsClientException(String message, List<Message> messages) {
        super(message);
        this.messages = messages;
    }

    /**
     * Creates a new exception.
     * @param message The message
     * @param cause   The cause
     */
    public NatsClientException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Creates a new exception.
     * @param message  The message
     * @param cause    The cause
     * @param messages The messages that failed to send
     */
    public NatsClientException(String message, Throwable cause, List<Message> messages) {
        super(message, cause);
        this.messages = messages;
    }

}
