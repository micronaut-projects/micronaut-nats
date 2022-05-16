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
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.Consumer
import spock.util.concurrent.PollingConditions

class ConsumerRegistrySpec extends AbstractNatsTest {

    void "test consumer registry"() {
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


    @Requires(property = "spec.name", value = "ConsumerRegistrySpec")
    @NatsClient
    static interface MyProducer {

        @Subject("pojo")
        void go(String person)

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
