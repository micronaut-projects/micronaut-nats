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
import io.nats.client.JetStreamManagement
import io.nats.client.api.PublishAck
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CopyOnWriteArrayList

class MultiFilterSpec extends AbstractJetstreamTest {

    void "push consumer with multiple filter subjects"() {
        given:
        ApplicationContext context = startContext([
                "nats.default.jetstream.streams.mf-events.storage-type": "Memory",
                "nats.default.jetstream.streams.mf-events.subjects"    : ["mf.>"],
        ])
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        JetStreamManagement jsm = context.getBean(JetStreamManagement)
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
        producer.send("mf.orders.1", "order".bytes)
        producer.send("mf.payments.1", "payment".bytes)
        producer.send("mf.other.1", "other".bytes)

        then:
        conditions.eventually {
            consumer.filtered*.toString().sort() == ["order", "payment"]
            consumer.filteredWithSubject*.toString().sort() == ["order", "payment"]
        }

        cleanup:
        jsm.deleteStream("mf-events")
        context.close()
    }

    @Requires(property = "spec.name", value = "MultiFilterSpec")
    @JetStreamClient
    static interface MyProducer {

        PublishAck send(@Subject String subject, @MessageBody byte[] data)
    }

    @Requires(property = "spec.name", value = "MultiFilterSpec")
    @JetStreamListener
    static class MyConsumer {

        List<String> filtered = new CopyOnWriteArrayList<>()

        List<String> filteredWithSubject = new CopyOnWriteArrayList<>()

        @PushConsumer(value = "mf-events", durable = "mf-filtered", filterSubjects = ["mf.orders.>", "mf.payments.>"])
        void listen(byte[] data) {
            filtered.add(new String(data))
        }

        @PushConsumer(value = "mf-events", subject = "mf.orders.>", durable = "mf-filtered-subject", filterSubjects = ["mf.orders.>", "mf.payments.>"])
        void listenWithSubject(byte[] data) {
            filteredWithSubject.add(new String(data))
        }
    }
}
