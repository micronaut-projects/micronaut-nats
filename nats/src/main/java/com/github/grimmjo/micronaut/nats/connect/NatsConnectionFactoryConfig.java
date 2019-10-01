/*
 * Copyright 2017-2019 original authors
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

package com.github.grimmjo.micronaut.nats.connect;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import io.micronaut.context.annotation.Parameter;

/**
 * Base class for nats to be configured.
 *
 * @author jgrimm
 * @since 1.0.0
 */
public abstract class NatsConnectionFactoryConfig {

    private final String name;

    private List<String> addresses = null;

    /**
     * Default constructor.
     *
     * @param name The name of the configuration
     */
    public NatsConnectionFactoryConfig(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name qualifier
     */
    public String getName() {
        return name;
    }

    /**
     * @return An optional list of addresses
     */
    public Optional<List<String>> getAddresses() {
        return Optional.ofNullable(addresses);
    }

    /**
     * Sets the addresses to be passed to {@link io.nats.client.Options.Builder#servers(String[])}}.
     *
     * @param addresses The list of addresses
     */
    public void setAddresses(@Nullable List<String> addresses) {
        this.addresses = addresses;
    }

}
