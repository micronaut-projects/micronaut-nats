/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.nats.exception;

import io.micronaut.messaging.exceptions.MessagingClientException;

/**
 * Exception thrown when an error occurs publishing a Nats message.
 *
 * @author jgrimm
 * @since 1.0.0
 */
public class NatsClientException extends MessagingClientException {

    /**
     * Creates a new exception.
     *
     * @param message The message
     */
    public NatsClientException(String message) {
        super(message);
    }
}
