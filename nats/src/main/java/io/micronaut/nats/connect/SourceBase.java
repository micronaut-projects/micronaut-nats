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

import java.time.ZonedDateTime;
import java.util.List;

abstract class SourceBase<T extends SubjectTransformBase, E extends External> {

    protected String name;
    protected long startSeq = 0;
    protected ZonedDateTime startTime;
    protected String filterSubject;
    protected E external;
    protected List<T> subjectTransforms;

    /**
     * Name.
     *
     * @param name {@link String}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Start sequence.
     *
     * @param startSeq long
     */
    public void setStartSeq(long startSeq) {
        this.startSeq = startSeq;
    }

    /**
     * Start time.
     *
     * @param startTime {@link ZonedDateTime}
     */
    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Filter subject.
     *
     * @param filterSubject {@link String}
     */
    public void setFilterSubject(String filterSubject) {
        this.filterSubject = filterSubject;
    }

    /**
     * Subject transformations.
     * @param subjectTransforms list
     */
    public void setSubjectTransforms(List<T> subjectTransforms) {
        this.subjectTransforms = subjectTransforms;
    }

    /**
     * External.
     * @param external {@link External}
     */
    public void setExternal(E external) {
        this.external = external;
    }

}
