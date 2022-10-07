/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.nats.jetstream.bind;

import java.time.Duration;
import java.util.Optional;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageAcknowledgementException;
import io.micronaut.nats.bind.NatsTypeArgumentBinder;
import io.nats.client.Message;
import jakarta.inject.Singleton;

/**
 * Binds an argument of type {@link Acknowledgement} from the {@link Message}.
 *
 * @param <T> Any type that extends {@link Acknowledgement}
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Singleton
public class AcknowledgementBinder<T extends Acknowledgement>
    implements NatsTypeArgumentBinder<T> {
    @Override
    public Argument<T> argumentType() {
        return (Argument<T>) Argument.of(Acknowledgement.class);
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, Message source) {
        Acknowledgement acknowledgement = new NatsAcknowledgement() {
            @Override
            public void ack() throws MessageAcknowledgementException {
                source.ack();
            }

            @Override
            public void nack() throws MessageAcknowledgementException {
                source.nak();
            }

            @Override
            public void nackWithDelay(Duration duration) {
                source.nakWithDelay(duration);
            }
        };

        return () -> Optional.of((T) acknowledgement);
    }
}
