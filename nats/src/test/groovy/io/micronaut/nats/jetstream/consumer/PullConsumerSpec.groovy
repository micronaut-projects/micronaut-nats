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
import io.nats.client.JetStreamManagement
import io.nats.client.PullSubscribeOptions
import io.nats.client.api.PublishAck

import java.time.Duration

class PullConsumerSpec extends AbstractJetstreamTest {

    void "simple pulling"() {
        given:
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)
        PullConsumerRegistry pullConsumerRegistry = context.getBean(PullConsumerRegistry)
        JetStreamManagement jsm = context.getBean(JetStreamManagement)

        when:
        producer.one("abc".bytes)

        then:
        def configuration = PullSubscribeOptions.builder()
                .stream("widgets")
                .durable("test1")
                .configuration(io.nats.client.api.ConsumerConfiguration.builder().ackWait(Duration.ofMillis(2500)).build())
        def streamSubscription = pullConsumerRegistry.newPullConsumer("subject.three", configuration.build())
        def fetch = streamSubscription.fetch(1, Duration.ofSeconds(3l))
        fetch.forEach { it.ack() }
        fetch.size() == 1

        streamSubscription.fetch(1, Duration.ofSeconds(3l)).size() == 0

        cleanup:
        jsm.purgeStream("widgets")
        context.close()
    }

    @Requires(property = "spec.name", value = "PullConsumerSpec")
    @JetStreamClient
    static interface MyProducer {

        @Subject("subject.three")
        PublishAck one(@MessageBody byte[] data)

    }

}
