/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.nats.connect

import io.micronaut.context.ApplicationContext
import io.micronaut.nats.jetstream.AbstractJetstreamTest
import io.nats.client.api.StorageType

import java.time.ZoneId
import java.time.ZonedDateTime

class ConfigSpec extends AbstractJetstreamTest {

    void "Stream config"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.streams.widgets.republish.source"                            : "subject.three",
                "nats.default.jetstream.streams.widgets.republish.destination"                       : "republish",
                "nats.default.jetstream.streams.widgets.republish.headers-only"                      : "true",
                "nats.default.jetstream.streams.widgets.subject-transform.source"                    : "*.*",
                "nats.default.jetstream.streams.widgets.subject-transform.destination"               : '$2.$1',
                "nats.default.jetstream.streams.widgets.placement.cluster"                           : "default",
                "nats.default.jetstream.streams.widgets.placement.tags"                              : ["tag1", "tag2"],
                "nats.default.jetstream.streams.widgets.sources[0].name"                             : "widgets",
                "nats.default.jetstream.streams.widgets.sources[0].filter-subject"                   : "subject.three",
                "nats.default.jetstream.streams.widgets.sources[0].startSeq"                         : 1,
                "nats.default.jetstream.streams.widgets.sources[0].startTime"                        : "1970-01-01T00:00:00Z[UTC]",
                "nats.default.jetstream.streams.widgets.sources[0].external.api"                     : "test",
                "nats.default.jetstream.streams.widgets.sources[0].external.deliver"                 : "deliver",
                "nats.default.jetstream.streams.widgets.sources[0].subject-transforms[0].source"     : "subject.*",
                "nats.default.jetstream.streams.widgets.sources[0].subject-transforms[0].destination": 'subject.test.$1',
                "nats.default.jetstream.streams.m-widgets.mirror.name"                               : "widgets",
                "nats.default.jetstream.streams.m-widgets.mirror.storage"                            : "memory",
                "nats.default.jetstream.streams.m-widgets.mirror.external.api"                       : "m-api",
                "nats.default.jetstream.streams.m-widgets.mirror.external.deliver"                   : "m-deliver",
                "nats.default.jetstream.streams.m-widgets.mirror.filter-subject"                     : "subject.three",
                "nats.default.jetstream.streams.m-widgets.mirror.startSeq"                           : 1,
                "nats.default.jetstream.streams.m-widgets.mirror.startTime"                          : "1970-01-01T00:00:00Z[UTC]",
        ])

        when:
        NatsConnectionFactoryConfig config = context.getBean(NatsConnectionFactoryConfig)

        then:
        config.getJetstream() != null
        config.getJetstream().getStreams() != null

        def widgetsConfiguration = config.getJetstream().getStreams().find { it.toStreamConfiguration().name == 'widgets' }


        widgetsConfiguration != null
        def configuration = widgetsConfiguration.toStreamConfiguration()

        // basics
        configuration.storageType == StorageType.Memory
        configuration.subjects.contains("subject.>")

        // republish
        configuration.republish.source == "subject.three"
        configuration.republish.destination == "republish"
        configuration.republish.headersOnly

        // subject transformation
        configuration.subjectTransform.source == '*.*'
        configuration.subjectTransform.destination == '$2.$1'

        // placement
        configuration.placement.cluster == "default"
        configuration.placement.tags.containsAll(["tag1", "tag2"])

        // Sources
        !configuration.sources.isEmpty()
        configuration.sources[0].external.api == "test"
        configuration.sources[0].external.deliver == "deliver"
        configuration.sources[0].startSeq == 1
        configuration.sources[0].startTime == ZonedDateTime.ofInstant(new Date(0).toInstant(), ZoneId.of("UTC"))
        configuration.sources[0].filterSubject == "subject.three"
        configuration.sources[0].subjectTransforms[0].source == "subject.*"
        configuration.sources[0].subjectTransforms[0].destination == 'subject.test.$1'

        // mirror
        def mirrorConfiguration = config.getJetstream().getStreams().find { it.toStreamConfiguration().name == 'm-widgets' }
        mirrorConfiguration != null
        def mirrorConf = mirrorConfiguration.toStreamConfiguration()
        mirrorConf.mirror.name == "widgets"
        mirrorConf.mirror.external.api == "m-api"
        mirrorConf.mirror.external.deliver == "m-deliver"
        mirrorConf.mirror.startSeq == 1
        mirrorConf.mirror.startTime == ZonedDateTime.ofInstant(new Date(0).toInstant(), ZoneId.of("UTC"))
        mirrorConf.mirror.filterSubject == "subject.three"


        cleanup:
        context.close()
    }

    void "KV config"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.keyvalue.examplebucket.republish.source"                            : "subject.three",
                "nats.default.jetstream.keyvalue.examplebucket.republish.destination"                       : "republish",
                "nats.default.jetstream.keyvalue.examplebucket.republish.headers-only"                      : "true",
                "nats.default.jetstream.keyvalue.examplebucket.placement.cluster"                           : "default",
                "nats.default.jetstream.keyvalue.examplebucket.placement.tags"                              : ["tag1", "tag2"],
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].name"                             : "widgets",
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].filter-subject"                   : "subject.three",
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].startSeq"                         : 1,
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].startTime"                        : "1970-01-01T00:00:00Z[UTC]",
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].external.api"                     : "test",
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].external.deliver"                 : "deliver",
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].subject-transforms[0].source"     : "subject.*",
                "nats.default.jetstream.keyvalue.examplebucket.sources[0].subject-transforms[0].destination": 'subject.test.$1',
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.name"                               : "widgets",
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.storage"                            : "memory",
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.external.api"                       : "m-api",
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.external.deliver"                   : "m-deliver",
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.filter-subject"                     : "subject.three",
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.startSeq"                           : 1,
                "nats.default.jetstream.keyvalue.m-examplebucket.mirror.startTime"                          : "1970-01-01T00:00:00Z[UTC]",
        ])

        when:
        NatsConnectionFactoryConfig config = context.getBean(NatsConnectionFactoryConfig)

        then:
        config.getJetstream() != null
        config.getJetstream().getKeyvalue() != null

        def bucketConf = config.getJetstream().getKeyvalue().find { it.toKeyValueConfiguration().bucketName == 'examplebucket' }


        bucketConf != null
        def configuration = bucketConf.toKeyValueConfiguration()

        // basics
        configuration.storageType == StorageType.Memory

        // republish
        configuration.republish.source == "subject.three"
        configuration.republish.destination == "republish"
        configuration.republish.headersOnly

        // placement
        configuration.placement.cluster == "default"
        configuration.placement.tags.containsAll(["tag1", "tag2"])

        // Sources
        !bucketConf.sources.isEmpty()
        bucketConf.sources[0].external.api == "test"
        bucketConf.sources[0].external.deliver == "deliver"
        bucketConf.sources[0].startSeq == 1
        bucketConf.sources[0].startTime == ZonedDateTime.ofInstant(new Date(0).toInstant(), ZoneId.of("UTC"))
        bucketConf.sources[0].filterSubject == "subject.three"
        bucketConf.sources[0].subjectTransforms[0].source == "subject.*"
        bucketConf.sources[0].subjectTransforms[0].destination == "subject.test.\$1"

        // mirror
        def mirrorConfiguration = config.getJetstream().getKeyvalue().find { it.toKeyValueConfiguration().bucketName == 'm-examplebucket' }
        mirrorConfiguration != null
        def mirrorConf = mirrorConfiguration.toKeyValueConfiguration()
        mirrorConfiguration.mirror.name == "widgets"
        mirrorConfiguration.mirror.external.api == "m-api"
        mirrorConfiguration.mirror.external.deliver == "m-deliver"
        mirrorConfiguration.mirror.startSeq == 1
        mirrorConfiguration.mirror.startTime == ZonedDateTime.ofInstant(new Date(0).toInstant(), ZoneId.of("UTC"))
        mirrorConfiguration.mirror.filterSubject == "subject.three"


        cleanup:
        context.close()
    }

    void "object store config"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.objectstore.examplestore.placement.cluster"                           : "default",
                "nats.default.jetstream.objectstore.examplestore.placement.tags"                              : ["tag1", "tag2"],
        ])

        when:
        NatsConnectionFactoryConfig config = context.getBean(NatsConnectionFactoryConfig)

        then:
        config.getJetstream() != null
        config.getJetstream().getObjectstore() != null

        def bucketConf = config.getJetstream().getObjectstore().get(0)


        bucketConf != null
        def configuration = bucketConf.toObjectStoreConfiguration()

        // basics
        configuration.storageType == StorageType.Memory

        // placement
        configuration.placement.cluster == "default"
        configuration.placement.tags.containsAll(["tag1", "tag2"])

        cleanup:
        context.close()
    }


}
