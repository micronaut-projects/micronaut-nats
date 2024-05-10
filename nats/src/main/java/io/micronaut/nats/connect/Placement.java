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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.List;

/**
 * Manages the placement of a stream, kv or object store.
 *
 * @author Joachim Grimm
 * @since 4.1.0
 */
public abstract class Placement {

    @NonNull
    private String cluster;

    private List<String> tags;

    /**
     * Builder of the placement object.
     *
     * @return builder
     */
    io.nats.client.api.Placement build() {
        return io.nats.client.api.Placement.builder().cluster(cluster).tags(tags).build();
    }

    /**
     * Cluster.
     * @param cluster {@link String}
     */
    public void setCluster(@NonNull String cluster) {
        this.cluster = cluster;
    }

    /**
     * Tags.
     * @param tags List of tags
     */
    public void setTags(@Nullable List<String> tags) {
        this.tags = tags;
    }
}
