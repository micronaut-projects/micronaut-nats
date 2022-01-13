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
import io.nats.client.impl.Headers;
import jakarta.inject.Singleton;

/**
 * Binds an argument of type {@link Headers} from the {@link Message}.
 *
 * @author Joachim Grimm
 * @since 3.1.0
 */
@Singleton
public class NatsHeadersBinder implements NatsTypeArgumentBinder<Headers> {

    @Override
    public Argument<Headers> argumentType() {
        return Argument.of(Headers.class);
    }

    @Override
    public BindingResult<Headers> bind(ArgumentConversionContext<Headers> context, Message source) {
        return () -> Optional.of(source.getHeaders());
    }
}
