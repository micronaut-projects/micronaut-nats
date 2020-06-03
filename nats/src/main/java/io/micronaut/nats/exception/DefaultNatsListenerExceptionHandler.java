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

import java.util.Optional;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Primary;
import io.nats.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default ExceptionHandler used when a {@link io.micronaut.nats.annotation.NatsListener} fails to process Nats message.
 * By defaul just logs the error
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
@Primary
public class DefaultNatsListenerExceptionHandler implements NatsListenerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNatsListenerExceptionHandler.class);

    @Override
    public void handle(NatsListenerException exception) {
        if (LOG.isErrorEnabled()) {
            Optional<Message> messageState = exception.getMessageState();
            if (messageState.isPresent()) {
                LOG.error("Error processing a message for nats listener [{}]", exception.getListener(), exception);
            } else {
                LOG.error("Nats listener [{}] produced an error", exception.getListener(), exception);
            }
        }
    }
}
