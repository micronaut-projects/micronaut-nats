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
 * Mirror configuration.
 *
 * @param <T> {@link SubjectTransformBase}
 * @author Joachim Grimm
 * @since 4.1.0
 */
public abstract class Mirror<T extends SubjectTransformBase> extends SourceBase<T> {

    /**
     * build the mirror object from the given configuration.
     *
     * @return this
     */
    public io.nats.client.api.Mirror build() {
        return io.nats.client.api.Mirror.builder()
            .name(getName())
            .external(getExternalBuilder().build())
            .filterSubject(getFilterSubject())
            .startSeq(getStartSeq())
            .startTime(getStartTime())
            .subjectTransforms(getSubjectTransforms().stream().map(SubjectTransformBase::build)
                .toList()).build();
    }
}
