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
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
import io.nats.client.api.PublishAck

class ExceptionConsumerSpec extends AbstractJetstreamTest {

    void "throw exception in consumer"() {
        when:
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)

        then:
        producer.one("abc".bytes)

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "ExceptionConsumerSpec")
    @JetStreamListener
    static class MyConsumer {

        @PushConsumer("widgets")
        @Subject("subject.>")
        void listen(byte[] data) {
            throw new IOException()
        }
    }

    @Requires(property = "spec.name", value = "ExceptionConsumerSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("subject.test")
        PublishAck one(@MessageBody byte[] data)

    }

}
