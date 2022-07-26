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
package io.micronaut.nats.jetstream

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.nats.annotation.NatsConnection
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.nats.client.JetStreamManagement
import io.nats.client.PublishOptions
import io.nats.client.api.PublishAck
import io.nats.client.api.StreamInfo
import spock.util.concurrent.PollingConditions

class JetStreamPublisherSpec extends AbstractJetstreamTest {

    void "simple publishing"() {
        given:
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        JetStreamManagement jsm = context.getBean(JetStreamManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))

        when:
        producer.go("abc".bytes, null)
        producer.go("def".bytes, null)

        then:
        conditions.eventually {
            StreamInfo streamInfo = jsm.getStreamInfo("widgets")
            streamInfo.streamState.msgCount == 2
        }

        cleanup:
        context.close()
    }

    void "simple publishing with options"() {
        given:
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        JetStreamManagement jsm = context.getBean(JetStreamManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))

        when:
        PublishAck pa = producer.go("abc".bytes, PublishOptions.builder()
                .stream("widgets")
                .messageId("id00001")
                .expectedStream("widgets")
                .build())
        producer.go("def".bytes, PublishOptions.builder()
                .stream("widgets")
                .messageId("id00002")
                .expectedStream("widgets")
                .expectedLastMsgId("id00001")
                .expectedLastSequence(pa.getSeqno())
                .build())

        then:
        conditions.eventually {
            StreamInfo streamInfo = jsm.getStreamInfo("widgets")
            streamInfo.streamState.msgCount == 4
        }

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "JetStreamPublisherSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("subject.one")
        PublishAck go(@MessageBody byte[] data, PublishOptions publishOptions)

    }
}
