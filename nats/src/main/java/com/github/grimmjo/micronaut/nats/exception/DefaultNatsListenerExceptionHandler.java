package com.github.grimmjo.micronaut.nats.exception;

import java.util.Optional;

import javax.inject.Singleton;

import io.micronaut.context.annotation.Primary;
import io.nats.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default ExceptionHandler used when a {@link com.github.grimmjo.micronaut.nats.annotation.NatsListener}
 * fails to process Nats message. By defaul just logs the error
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
@Primary
public class DefaultNatsListenerExceptionHandler implements NatsListenerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNatsListenerExceptionHandler.class);

    @Override
    public void handle(NatsListenerException exception) {
        if (logger.isErrorEnabled()) {
            Optional<Message> messageState = exception.getMessageState();
            if (messageState.isPresent()) {
                logger.error("Error processing a message for nats listener [{}]", exception.getListener(), exception);
            } else {
                logger.error("Nats listener [{}] produced an error", exception.getListener(), exception);
            }
        }
    }
}
