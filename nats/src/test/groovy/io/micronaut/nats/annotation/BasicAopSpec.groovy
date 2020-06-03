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
import io.micronaut.messaging.annotation.Body
import io.micronaut.nats.AbstractNatsTest
import io.reactivex.Completable
import spock.util.concurrent.PollingConditions
/**
 *
 * @author jgrimm
 */
class BasicAopSpec extends AbstractNatsTest {

    void "test simple producing and consuming"() {
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
        producer.go("abc".bytes)
        producer.go("def".bytes)
//        boolean success = producer.goConfirm("def".bytes).blockingAwait(2, TimeUnit.SECONDS)

        then:
        conditions.eventually {
            consumer.messages.size() == 2
            consumer.messages[0] == "abc".bytes
            consumer.messages[1] == "def".bytes
        }

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "BasicAopSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("abc")
        void go(@Body byte[] data)

        @Subject("abc")
        Completable goConfirm(byte[] data)
    }

    @Requires(property = "spec.name", value = "BasicAopSpec")
    @NatsListener
    static class MyConsumer {

        public static List<byte[]> messages = []

        @Subject("abc")
        void listen(byte[] data) {
            messages.add(data)
        }
    }
}
