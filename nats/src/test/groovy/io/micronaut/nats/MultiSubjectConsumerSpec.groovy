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
package io.micronaut.nats

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.intercept.NatsConsumerAdvice
import spock.util.concurrent.PollingConditions

class MultiSubjectConsumerSpec extends AbstractNatsTest {

    void "all subject methods of a listener are registered and closed"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = applicationContext.getBean(MyProducer)
        ConsumerRegistry registry = applicationContext.getBean(ConsumerRegistry)

        expect:
        registry.getConsumerSubscription("multi-client")*.subject.toSet() == ["multi.a", "multi.b"].toSet()

        when:
        producer.a("1")
        producer.b("1")

        then:
        conditions.eventually {
            MyConsumer.a == ["1"]
            MyConsumer.b == ["1"]
        }

        when:
        applicationContext.getBean(NatsConsumerAdvice).close()
        producer.a("2")
        producer.b("2")
        sleep(500)

        then:
        MyConsumer.a == ["1"]
        MyConsumer.b == ["1"]

        cleanup:
        applicationContext.close()
    }

    @Requires(property = "spec.name", value = "MultiSubjectConsumerSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("multi.a")
        void a(String data)

        @Subject("multi.b")
        void b(String data)
    }

    @Requires(property = "spec.name", value = "MultiSubjectConsumerSpec")
    @NatsListener(clientId = "multi-client")
    static class MyConsumer {

        static List<String> a = [].asSynchronized()
        static List<String> b = [].asSynchronized()

        @Subject("multi.a")
        void onA(String data) {
            a.add(data)
        }

        @Subject("multi.b")
        void onB(String data) {
            b.add(data)
        }
    }
}
