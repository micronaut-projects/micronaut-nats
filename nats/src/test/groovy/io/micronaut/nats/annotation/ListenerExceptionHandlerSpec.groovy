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
package io.micronaut.nats.annotation

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.nats.AbstractNatsTest
import io.micronaut.nats.exception.NatsListenerException
import io.micronaut.nats.exception.NatsListenerExceptionHandler
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CopyOnWriteArrayList

class ListenerExceptionHandlerSpec extends AbstractNatsTest {

    void "test exception thrown by listener method is passed to the exception handler"() {
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
        producer.go("abc")

        then:
        conditions.eventually {
            consumer.errors.size() == 1
            consumer.errors[0].cause instanceof IllegalStateException
            consumer.errors[0].cause.message == "boom abc"
            consumer.errors[0].messageState.isPresent()
        }

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "ListenerExceptionHandlerSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("exception-handler")
        void go(@MessageBody String data)
    }

    @Requires(property = "spec.name", value = "ListenerExceptionHandlerSpec")
    @NatsListener
    static class MyConsumer implements NatsListenerExceptionHandler {

        List<NatsListenerException> errors = new CopyOnWriteArrayList<>()

        @Subject("exception-handler")
        void listen(String data) {
            throw new IllegalStateException("boom " + data)
        }

        @Override
        void handle(NatsListenerException exception) {
            errors.add(exception)
        }
    }
}
