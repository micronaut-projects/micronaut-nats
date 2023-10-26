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
package io.micronaut.nats.jetstream.consumer

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.jetstream.AbstractJetstreamTest
import io.micronaut.nats.jetstream.PullConsumerRegistry
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
import io.nats.client.JetStreamManagement
import io.nats.client.JetStreamSubscription
import io.nats.client.PullSubscribeOptions
import io.nats.client.api.AckPolicy
import io.nats.client.api.PublishAck
import spock.util.concurrent.PollingConditions

import java.time.Duration

class SourcesSpec extends AbstractJetstreamTest {

    void "build stream from other stream"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.streams.d-widgets.storage-type": "Memory",
                "nats.default.jetstream.streams.d-widgets.sources[0].name" : "widgets",
                "nats.default.jetstream.streams.d-widgets.sources[0].filter-subject" : "subject.three",
                "nats.default.jetstream.streams.d-widgets.sources[0].external.api" : "test",
                "nats.default.jetstream.streams.d-widgets.sources[0].subject-transforms[0].source" : "subject.*",
                "nats.default.jetstream.streams.d-widgets.sources[0].subject-transforms[0].destination" : 'subject.test.$1',
        ])
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PullConsumerRegistry pullConsumerRegistry = context.getBean(PullConsumerRegistry)
        JetStreamManagement jsm = context.getBean(JetStreamManagement)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        PullSubscribeOptions configuration = PullSubscribeOptions.builder()
                .stream("d-widgets")
                .durable("test1")
                .build()
        JetStreamSubscription streamSubscription = pullConsumerRegistry.newPullConsumer("subject.test.three", configuration)

        when:
        producer.one("subject.three","abc".bytes)
        producer.one("subject.two","abc".bytes)

        then:
        conditions.eventually {
            def fetch = streamSubscription.fetch(1, Duration.ofSeconds(3l))
            fetch.forEach { it.ack() }
            fetch.size() == 1

            consumer.messages.size() == 2
            consumer.messagesDestination.size() == 1
        }


        cleanup:
        jsm.deleteStream("widgets")
        jsm.deleteStream("d-widgets")
        context.close()
    }

    @Requires(property = "spec.name", value = "SourcesSpec")
    @JetStreamClient
    static interface MyProducer {

        PublishAck one(@Subject String subject, @MessageBody byte[] data)

    }

    @Requires(property = "spec.name", value = "SourcesSpec")
    @JetStreamListener
    static class MyConsumer {

        public static List<byte[]> messagesDestination = []

        @PushConsumer(value = "d-widgets", subject = "subject.test.three", durable = "test", ackPolicy = AckPolicy.All)
        void listenDestination(byte[] data) {
            messagesDestination.add(data)
        }

        public static List<byte[]> messages = []

        @PushConsumer(value = "widgets", subject = "subject.>", durable = "test", ackPolicy = AckPolicy.All)
        void listenOrigin(byte[] data) {
            messages.add(data)
        }
    }

}
