/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.nats.serdes;

import java.util.Arrays;
import java.util.Optional;

import javax.inject.Singleton;

import io.micronaut.core.type.Argument;

/**
 * Default implementation of {@link NatsMessageSerDesRegistry}.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class DefaultNatsMessageSerDesRegistry implements NatsMessageSerDesRegistry {

    private final NatsMessageSerDes<?>[] serDes;

    /**
     * Default constructor.
     *
     * @param serDes The serdes to be registered.
     */
    public DefaultNatsMessageSerDesRegistry(NatsMessageSerDes<?>... serDes) {
        this.serDes = serDes;
    }

    @Override
    public <T> Optional<NatsMessageSerDes<T>> findSerdes(Argument<T> type) {
        return Arrays.stream(serDes)
                .filter(serDes -> serDes.supports((Argument) type))
                .map(serDes -> (NatsMessageSerDes<T>) serDes)
                .findFirst();
    }
}
