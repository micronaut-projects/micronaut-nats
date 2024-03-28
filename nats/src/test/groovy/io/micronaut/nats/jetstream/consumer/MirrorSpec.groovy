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

class MirrorSpec extends AbstractJetstreamTest {

    void "simple mirror pull"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.streams.m-widgets.storage-type": "Memory",
                "nats.default.jetstream.streams.m-widgets.mirror.name" : "widgets",
        ])
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PullConsumerRegistry pullConsumerRegistry = context.getBean(PullConsumerRegistry)
        JetStreamManagement jsm = context.getBean(JetStreamManagement)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        PullSubscribeOptions configuration = PullSubscribeOptions.builder()
                .stream("m-widgets")
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
            consumer.messagesMirror.size() == 1
        }


        cleanup:
        jsm.deleteStream("widgets")
        jsm.deleteStream("m-widgets")
        context.close()
    }

    @Requires(property = "spec.name", value = "MirrorSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("subject.three")
        PublishAck one(@MessageBody byte[] data)

    }

    @Requires(property = "spec.name", value = "MirrorSpec")
    @JetStreamListener
    static class MyConsumer {

        public static List<byte[]> messagesMirror = []

        @PushConsumer(value = "m-widgets", subject = "subject.three", durable = "test", ackPolicy = AckPolicy.All)
        void listenMirror(byte[] data) {
            messagesMirror.add(data)
        }

        public static List<byte[]> messages = []

        @PushConsumer(value = "widgets", subject = "subject.three", durable = "test", ackPolicy = AckPolicy.All)
        void listenOrigin(byte[] data) {
            messages.add(data)
        }
    }

}
