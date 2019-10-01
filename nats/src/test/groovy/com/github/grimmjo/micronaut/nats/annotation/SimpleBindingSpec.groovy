/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.grimmjo.micronaut.nats.annotation

import com.github.grimmjo.micronaut.nats.AbstractNatsTest
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import spock.util.concurrent.PollingConditions

import javax.annotation.Nullable

class SimpleBindingSpec extends AbstractNatsTest {

    void "test simple producing and consuming with a boolean"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = applicationContext.getBean(MyProducer)
        MyConsumer consumer = applicationContext.getBean(MyConsumer)

        when:
        producer.go(true)
        producer.go(false)
        producer.go(null)

        then:
        conditions.eventually {
            consumer.messages.size() == 3
            consumer.messages[0] == true
            consumer.messages[1] == false
            consumer.messages[2] == null
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
        producer.go([true, false])
        producer.go([null, true])

        then:
        conditions.eventually {
            consumer.messages.size() == 4
            consumer.messages[0] == true
            consumer.messages[1] == false
            consumer.messages[2] == null
            consumer.messages[3] == true
        }

        cleanup:
        applicationContext.close()
    }

    static class Person {
        String name
    }

    @Requires(property = "spec.name", value = "SimpleBindingSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("simple")
        void go(Boolean data)

    }

    @Requires(property = "spec.name", value = "SimpleBindingSpec")
    @NatsListener
    static class MyConsumer {

        public static List<Boolean> messages = []

        @Subject("simple")
        void listen(@Nullable Boolean data) {
            messages.add(data)
        }
    }

    @Requires(property = "spec.name", value = "SimpleBindingSpec")
    @NatsClient
    static interface MyListProducer {

        @Subject("simple-list")
        void go(List<Boolean> data)

    }

    @Requires(property = "spec.name", value = "SimpleBindingSpec")
    @NatsListener
    static class MyListConsumer {

        public static List<Boolean> messages = []

        @Subject("simple-list")
        void listen(List<Boolean> data) {
            messages.addAll(data)
        }
    }
}
