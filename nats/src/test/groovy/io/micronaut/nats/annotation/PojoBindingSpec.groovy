/*
 * Copyright 2017-2019 original authors
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
package io.micronaut.nats.annotation


import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.nats.AbstractNatsTest
import io.micronaut.serde.annotation.Serdeable
import spock.util.concurrent.PollingConditions

class PojoBindingSpec extends AbstractNatsTest {

    void "test simple producing and consuming with a pojo"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = applicationContext.getBean(MyProducer)
        MyConsumer consumer = applicationContext.getBean(MyConsumer)

        when:
        producer.go(new Person(name: "abc"))

        then:
        conditions.eventually {
            consumer.messages.size() == 1
            consumer.messages[0].name == "abc"
        }

        cleanup:
        applicationContext.close()
    }

    void "test simple producing and consuming with a list of pojos"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyListProducer producer = applicationContext.getBean(MyListProducer)
        MyListConsumer consumer = applicationContext.getBean(MyListConsumer)

        when:
        producer.go([new Person(name: "abc"), new Person(name: "def")])

        then:
        conditions.eventually {
            consumer.messages.size() == 2
            consumer.messages[0].name == "abc"
            consumer.messages[1].name == "def"
        }

        cleanup:
        applicationContext.close()
    }

    @Serdeable
    static class Person {
        String name
    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("pojo")
        void go(Person data)

    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsListener
    static class MyConsumer {

        public static List<Person> messages = []

        @Subject("pojo")
        void listen(Person data) {
            messages.add(data)
        }
    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsClient
    static interface MyListProducer {

        @Subject("pojo-list")
        void go(List<Person> data)

    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsListener
    static class MyListConsumer {

        public static List<Person> messages = []

        @Subject("pojo-list")
        void listen(List<Person> data) {
            messages.addAll(data)
        }
    }
}
