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
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.jetstream.AbstractJetstreamTest
import io.micronaut.nats.jetstream.PullConsumerRegistry
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.nats.client.JetStreamManagement
import io.nats.client.JetStreamSubscription
import io.nats.client.PullSubscribeOptions
import io.nats.client.api.PublishAck
import spock.util.concurrent.PollingConditions

import java.time.Duration

class RepublishSpec extends AbstractJetstreamTest {

    void "simple republish"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.streams.widgets.republish.source": "subject.three",
                "nats.default.jetstream.streams.widgets.republish.destination": "republish",
                "nats.default.jetstream.streams.widgets.republish.headers-only": "false",
        ])
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PullConsumerRegistry pullConsumerRegistry = context.getBean(PullConsumerRegistry)
        JetStreamManagement jsm = context.getBean(JetStreamManagement)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        PullSubscribeOptions configuration = PullSubscribeOptions.builder()
                .stream("widgets")
                .durable("test1")
                .build()
        JetStreamSubscription streamSubscription = pullConsumerRegistry.newPullConsumer("subject.three", configuration)

        when:
        producer.one("abc".bytes)

        then:
        conditions.eventually {
            def fetch = streamSubscription.fetch(1, Duration.ofSeconds(3l))
            fetch.forEach { it.ack() }
            fetch.size() == 1

            consumer.messages.size() == 1
        }


        cleanup:
        jsm.deleteStream("widgets")
        context.close()
    }

    @Requires(property = "spec.name", value = "RepublishSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("subject.three")
        PublishAck one(@MessageBody byte[] data)

    }

    @Requires(property = "spec.name", value = "RepublishSpec")
    @NatsListener
    static class MyConsumer {

        public static List<byte[]> messages = []

        @Subject(value = "republish")
        void listenRepublish(byte[] data) {
            messages.add(data)
        }
    }

}
