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
package io.micronaut.nats.annotation


import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.nats.AbstractNatsTest
import spock.util.concurrent.PollingConditions

import javax.annotation.Nullable

class QueueSpec extends AbstractNatsTest {

    void "test simple producing and consuming with a boolean"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = applicationContext.getBean(MyProducer)
        MyConsumer consumer = applicationContext.getBean(MyConsumer)
        MySecondConsumer mySecondConsumer = applicationContext.getBean(MySecondConsumer)

        when:
        producer.go(true)

        then:
        conditions.eventually {
            if (consumer.messages.size() == 1) {
                mySecondConsumer.messages.size() == 0
            } else if (mySecondConsumer.messages.size() == 1) {
                consumer.messages.size() == 0
            } else {
                throw new Exception()
            }
        }

        cleanup:
        applicationContext.close()
    }

    @Requires(property = "spec.name", value = "QueueSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("simple")
        void go(Boolean data)

    }

    @Requires(property = "spec.name", value = "QueueSpec")
    @NatsListener
    static class MyConsumer {

        public static List<Boolean> messages = []

        @Subject(value = "simple", queue = "myQueue")
        void listen(@Nullable Boolean data) {
            messages.add(data)
        }
    }


    @Requires(property = "spec.name", value = "QueueSpec")
    @NatsListener
    static class MySecondConsumer {

        public static List<Boolean> messages = []

        @Subject(value = "simple", queue = "myQueue")
        void listen(@Nullable Boolean data) {
            messages.addAll(data)
        }
    }
}
