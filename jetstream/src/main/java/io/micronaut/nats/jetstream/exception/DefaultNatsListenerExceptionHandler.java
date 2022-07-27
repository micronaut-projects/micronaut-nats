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

import io.micronaut.context.annotation.Primary;
import io.nats.client.Message;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default ExceptionHandler used when a
 * {@link io.micronaut.nats.jetstream.annotation.JetStreamListener}
 * fails to process Nats message.
 * By defaul just logs the error
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Singleton
@Primary
public class DefaultNatsListenerExceptionHandler implements JetStreamListenerExceptionHandler {

    private static final Logger LOG =
        LoggerFactory.getLogger(DefaultNatsListenerExceptionHandler.class);

    @Override
    public void handle(JetStreamListenerException exception) {
        if (LOG.isErrorEnabled()) {
            Optional<Message> messageState = exception.getMessageState();
            if (messageState.isPresent()) {
                LOG.error("Error processing a message for JetStream listener [{}]",
                    exception.getListener(), exception);
            } else {
                LOG.error("JetStream listener [{}] produced an error", exception.getListener(),
                    exception);
            }
        }
    }
}
