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
import io.micronaut.nats.jetstream.intercept.JetStreamPushConsumerAdvice
import io.nats.client.api.PublishAck
import spock.util.concurrent.PollingConditions

class MultiSubjectPushConsumerSpec extends AbstractJetstreamTest {

    void "all push consumer methods of a listener are closed"() {
        ApplicationContext context = startContext(
                ["nats.default.jetstream.streams.multi.storage-type": "Memory",
                 "nats.default.jetstream.streams.multi.subjects"    : ["multi.>"]])
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = context.getBean(MyProducer)

        when:
        producer.a("1".bytes)
        producer.b("1".bytes)

        then:
        conditions.eventually {
            MyConsumer.a == ["1"]
            MyConsumer.b == ["1"]
        }

        when:
        context.getBean(JetStreamPushConsumerAdvice).close()
        producer.a("2".bytes)
        producer.b("2".bytes)
        sleep(500)

        then:
        MyConsumer.a == ["1"]
        MyConsumer.b == ["1"]

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "MultiSubjectPushConsumerSpec")
    @JetStreamListener(clientId = "multi-push-client")
    static class MyConsumer {

        static List<String> a = [].asSynchronized()
        static List<String> b = [].asSynchronized()

        @PushConsumer("multi")
        @Subject("multi.a")
        void onA(byte[] data) {
            a.add(new String(data))
        }

        @PushConsumer("multi")
        @Subject("multi.b")
        void onB(byte[] data) {
            b.add(new String(data))
        }
    }

    @Requires(property = "spec.name", value = "MultiSubjectPushConsumerSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("multi.a")
        PublishAck a(@MessageBody byte[] data)

        @Subject("multi.b")
        PublishAck b(@MessageBody byte[] data)
    }
}
