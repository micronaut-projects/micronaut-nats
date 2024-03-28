/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.nats.connect;

/**
 * External.
 *
 * @author Joachim Grimm
 * @since 4.1.0
 */
public abstract class External {

    private String api;
    private String deliver;

    /**
     * build the external information from the given configuration.
     *
     * @return this
     */
    public io.nats.client.api.External build() {
        return io.nats.client.api.External.builder().api(api).deliver(deliver).build();
    }

    /**
     * Api.
     * @param api {@link String}
     */
    public void setApi(String api) {
        this.api = api;
    }

    /**
     * Deliver.
     * @param deliver {@link String}
     */
    public void setDeliver(String deliver) {
        this.deliver = deliver;
    }

    final String getApi() {
        return api;
    }

    final String getDeliver() {
        return deliver;
    }
}
