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

import io.nats.client.api.SubjectTransform;

/**
 * Subject Tranformation.
 * @author Joachim Grimm
 * @since 4.1.0
 */
public abstract class SubjectTransformBase {

    private String source;

    private String destination;

    /**
     * build the Subject Transform object.
     * @return this
     */
    SubjectTransform build() {
        return SubjectTransform.builder().source(source).destination(destination).build();
    }

    /**
     * Source.
     * @return source
     */
    public String getSource() {
        return source;
    }

    /**
     * Source.
     * @param source {@link String}
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Destination.
     * @return destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Destination.
     * @param destination {@link String}
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
}
