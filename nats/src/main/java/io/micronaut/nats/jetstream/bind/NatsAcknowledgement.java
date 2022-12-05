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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.messaging.Acknowledgement;

import java.time.Duration;

/**
 * A contract for acknowleding or rejecting messages.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
public interface NatsAcknowledgement extends Acknowledgement {

    /**
     * nak acknowledges a JetStream message has been received but indicates that the message is not completely processed and should be sent again later, after at least the delay amount.
     *
     * @param duration {@link Duration}
     */
    void nackWithDelay(@NonNull Duration duration);

}
