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

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Named;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.nats.client.Options;

/**
 * The default Nats configuration class.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@ConfigurationProperties("nats")
@Named(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)
public class SingleNatsConnectionFactoryConfig extends NatsConnectionFactoryConfig {

    public static final String DEFAULT_NAME = "default";

    private static final String DEFAULT_URL = Options.DEFAULT_URL;

    private String address = DEFAULT_URL;

    /**
     * Default constructor.
     */
    public SingleNatsConnectionFactoryConfig() {
        super(DEFAULT_NAME);
    }

    /**
     * @return the address
     */
    public Optional<String> getAddress() {
        return Optional.ofNullable(address);
    }

    /**
     * sets the address.
     * @param address {@link String}
     */
    public void setAddress(@Nullable String address) {
        this.address = address;
    }
}
