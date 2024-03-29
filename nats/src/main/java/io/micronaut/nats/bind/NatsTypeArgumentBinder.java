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

import io.micronaut.core.bind.TypeArgumentBinder;
import io.nats.client.Message;

/**
 * An interface for Nats argument binding based on argument type.
 *
 * @param <T> The type of argument that can be bound
 * @author James Kleeh
 * @author Joachim Grimm
 * @since 3.1.0
 */
public interface NatsTypeArgumentBinder<T> extends TypeArgumentBinder<T, Message>, NatsArgumentBinder<T> {
}
