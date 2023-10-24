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

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.NonNull;
import io.nats.client.api.External;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

abstract class SourceBase<T extends SubjectTransformBase> {

    private String name;
    private long startSeq;
    private ZonedDateTime startTime;
    private String filterSubject;
    @ConfigurationBuilder(prefixes = "", excludes = {"build"}, configurationPrefix = "external")
    private External.Builder externalBuilder = External.builder();
    private List<T> subjectTransforms = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartSeq() {
        return startSeq;
    }

    public void setStartSeq(long startSeq) {
        this.startSeq = startSeq;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public String getFilterSubject() {
        return filterSubject;
    }

    public void setFilterSubject(String filterSubject) {
        this.filterSubject = filterSubject;
    }

    public List<T> getSubjectTransforms() {
        return subjectTransforms;
    }

    public void setSubjectTransforms(@NonNull List<T> subjectTransforms) {
        this.subjectTransforms = subjectTransforms;
    }

    public External.Builder getExternalBuilder() {
        return externalBuilder;
    }

    @EachProperty(value = "subject-transforms", list = true)
    public static class SubjectTransform extends SubjectTransformBase {
    }

}
