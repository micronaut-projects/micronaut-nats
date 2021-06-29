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
package io.micronaut.nats.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.nats.client.Message;
import jakarta.inject.Singleton;

/**
 * The default binder for binding an argument from the {@link Message}
 * that is used if no other binder supports the argument.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsDefaultBinder implements NatsArgumentBinder<Object> {

    private final NatsBodyBinder bodyBinder;

    /**
     * Default constructor.
     *
     * @param bodyBinder Bodybinder
     */
    public NatsDefaultBinder(NatsBodyBinder bodyBinder) {
        this.bodyBinder = bodyBinder;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, Message messageState) {
        return bodyBinder.bind(context, messageState);
    }
}
