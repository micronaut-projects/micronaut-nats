/*
 * Copyright 2017-2026 original authors
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
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
import io.nats.client.api.PublishAck
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CopyOnWriteArrayList

class ListenerQueueSpec extends AbstractJetstreamTest {

    void "the queue of @JetStreamListener is used for push consumers without own queue"() {
        given:
        ApplicationContext context = startContext(
                ["nats.default.jetstream.streams.queued.storage-type": "Memory",
                 "nats.default.jetstream.streams.queued.subjects"    : ['queued.>']])
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        MySecondConsumer secondConsumer = context.getBean(MySecondConsumer)
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
        producer.send("abc".bytes)

        then:
        conditions.eventually {
            assert consumer.messages.size() + secondConsumer.messages.size() >= 1
        }

        when:
        sleep(500)

        then:
        consumer.messages.size() + secondConsumer.messages.size() == 1

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "ListenerQueueSpec")
    @JetStreamListener(queue = "listener-queue")
    static class MyConsumer {

        List<byte[]> messages = new CopyOnWriteArrayList<>()

        @PushConsumer(value = "queued", durable = "listener-queue")
        @Subject("queued.listener-queue")
        void listen(byte[] data) {
            messages.add(data)
        }
    }

    @Requires(property = "spec.name", value = "ListenerQueueSpec")
    @JetStreamListener(queue = "listener-queue")
    static class MySecondConsumer {

        List<byte[]> messages = new CopyOnWriteArrayList<>()

        @PushConsumer(value = "queued", durable = "listener-queue")
        @Subject("queued.listener-queue")
        void listen(byte[] data) {
            messages.add(data)
        }
    }

    @Requires(property = "spec.name", value = "ListenerQueueSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("queued.listener-queue")
        PublishAck send(@MessageBody byte[] data)
    }
}
