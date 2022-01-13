/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.nats.bind;

import java.util.Optional;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.nats.client.Message;
import io.nats.client.Subscription;
import jakarta.inject.Singleton;

/**
 * Binds an argument of type {@link Subscription} from the {@link Message}.
 *
 * @author Joachim Grimm
 * @since 3.1.0
 */
@Singleton
public class NatsSubscriptionBinder implements NatsTypeArgumentBinder<Subscription> {

    @Override
    public Argument<Subscription> argumentType() {
        return Argument.of(Subscription.class);
    }

    @Override
    public BindingResult<Subscription> bind(ArgumentConversionContext<Subscription> context, Message source) {
        Optional<Subscription> subscription = Optional.of(source.getSubscription());
        return () -> subscription;
    }
}
