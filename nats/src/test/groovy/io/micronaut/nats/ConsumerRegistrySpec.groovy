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
package io.micronaut.nats

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.NoSuchBeanException
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.Consumer
import io.nats.client.Message
import io.nats.client.Subscription
import spock.util.concurrent.PollingConditions

import java.nio.charset.StandardCharsets
import java.time.Duration

class ConsumerRegistrySpec extends AbstractNatsTest {

    void "consumer registry"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = applicationContext.getBean(MyProducer)
        MyConsumer listener = applicationContext.getBean(MyConsumer)
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        when:
        Consumer consumer = registry.getConsumer("person-client")

        then:
        consumer
        consumer.deliveredCount == 0
        registry.getConsumerIds().contains("person-client")
        registry.getConsumerSubscription("person-client").size() == 1

        when:
        producer.go("abc")

        then:
        conditions.eventually {
            listener.messages.size() == 1
            listener.messages[0] == "abc"
            consumer.deliveredCount == 1
        }

        cleanup:
        applicationContext.close()
    }

    void "new temporary subscription"() {
        ApplicationContext applicationContext = startContext()
        MyProducer producer = applicationContext.getBean(MyProducer)
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        when:
        Subscription subscription = registry.newSubscription("temp", null)

        then:
        subscription

        when:
        producer.sendTemporary("abc")

        then:
        Message message = subscription.nextMessage(Duration.ofSeconds(1))
        message != null
        new String(message.data, StandardCharsets.UTF_8) == "abc"
    }

    void "new temporary subscription with queue"() {
        ApplicationContext applicationContext = startContext()
        MyProducer producer = applicationContext.getBean(MyProducer)
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        when:
        Subscription subscription1 = registry.newSubscription("temp", "queue")
        Subscription subscription2 = registry.newSubscription("temp", "queue")

        then:
        subscription1
        subscription2

        when:
        producer.sendTemporary("abc")

        then:
        Message message1 = subscription1.nextMessage(Duration.ofSeconds(1))
        Message message2 = subscription2.nextMessage(Duration.ofSeconds(1))
        message1 != null || message2 != null
    }

    void "new temporary subscription with unknown connection"() {
        ApplicationContext applicationContext = startContext()
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        when:
        registry.newSubscription("unknown", "temp", "queue")

        then:
        thrown NoSuchBeanException

        when:
        registry.newSubscription("unknown", "temp", null)

        then:
        thrown NoSuchBeanException
    }

    void "get unknown consumer"() {
        ApplicationContext applicationContext = startContext()
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        when:
        registry.getConsumer("unknown-client")

        then:
        thrown IllegalArgumentException
    }

    void "get unknown consumer subscriptions"() {
        ApplicationContext applicationContext = startContext()
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        when:
        registry.getConsumerSubscription("unknown-client")

        then:
        thrown IllegalArgumentException
    }


    @Requires(property = "spec.name", value = "ConsumerRegistrySpec")
    @NatsClient
    static interface MyProducer {

        @Subject("pojo")
        void go(String person)

        @Subject("temp")
        void sendTemporary(String person)

    }

    @Requires(property = "spec.name", value = "ConsumerRegistrySpec")
    @NatsListener(clientId = "person-client")
    static class MyConsumer {

        public static List<String> messages = []

        @Subject("pojo")
        void listen(String data) {
            messages.add(data)
        }
    }

}
