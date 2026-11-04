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
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Nullable
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.AbstractNatsTest
import io.micronaut.nats.exception.DefaultNatsListenerExceptionHandler
import io.micronaut.nats.exception.NatsListenerException
import io.micronaut.nats.exception.NatsListenerExceptionHandler
import io.nats.client.Connection
import jakarta.inject.Singleton
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.util.concurrent.PollingConditions

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

class ListenerExceptionHandlerSpec extends AbstractNatsTest {

    @Shared
    @AutoCleanup
    ApplicationContext context = startContext()

    PollingConditions conditions = new PollingConditions(timeout: 3)

    void "test exception thrown by listener method is passed to the listener's exception handler"() {
        given:
        MyProducer producer = context.getBean(MyProducer)
        HandlingConsumer consumer = context.getBean(HandlingConsumer)

        when:
        producer.go("abc")

        then:
        conditions.eventually {
            consumer.errors.size() == 1
            consumer.errors[0].message == "An error occurred processing the message"
            consumer.errors[0].cause instanceof IllegalStateException
            consumer.errors[0].cause.message == "boom abc"
            consumer.errors[0].messageState.isPresent()
            consumer.errors[0].listener.is(consumer)
        }
    }

    void "test exception thrown by listener method is passed to the default exception handler"() {
        given:
        MyProducer producer = context.getBean(MyProducer)
        NonHandlingConsumer consumer = context.getBean(NonHandlingConsumer)
        RecordingExceptionHandler handler = context.getBean(RecordingExceptionHandler)

        when:
        producer.goDefault("def")

        then:
        conditions.eventually {
            handler.errors.any { it.listener.is(consumer) && it.cause.message == "boom def" }
        }
    }

    void "test missing serializer for the reply is passed to the exception handler"() {
        given:
        Connection connection = context.getBean(Connection)
        HandlingConsumer consumer = context.getBean(HandlingConsumer)

        when:
        connection.publish("exception-handler-uri", "reply-inbox", "abc".getBytes(StandardCharsets.UTF_8))

        then:
        conditions.eventually {
            consumer.errors.any {
                it.cause == null && it.message.startsWith("Could not find a serializer") && it.messageState.isPresent()
            }
        }
    }

    void "test reply is published when the listener returns a value"() {
        given:
        Connection connection = context.getBean(Connection)

        expect:
        new String(connection.request("exception-handler-reply", "abc".getBytes(StandardCharsets.UTF_8),
                Duration.ofSeconds(3)).data, StandardCharsets.UTF_8) == "ABC"
    }

    void "test empty reply is published when the listener returns null"() {
        given:
        Connection connection = context.getBean(Connection)

        expect:
        connection.request("exception-handler-reply", new byte[0], Duration.ofSeconds(3)).data.length == 0
    }

    void "test binding error is passed to the exception handler"() {
        given:
        Connection connection = context.getBean(Connection)
        HandlingConsumer consumer = context.getBean(HandlingConsumer)

        when:
        connection.publish("exception-handler-header", "abc".getBytes(StandardCharsets.UTF_8))

        then:
        conditions.eventually {
            consumer.errors.any {
                it.message == "An error occurred binding the message to the method" && it.messageState.isPresent()
            }
        }
    }

    void "test no reply is published when the message has no reply subject"() {
        given:
        Connection connection = context.getBean(Connection)
        HandlingConsumer consumer = context.getBean(HandlingConsumer)
        int errorCount = consumer.errors.size()

        when:
        connection.publish("exception-handler-uri", "abc".getBytes(StandardCharsets.UTF_8))

        then:
        conditions.eventually {
            consumer.uris.contains("abc")
        }
        consumer.errors.size() == errorCount
    }

    @Requires(property = "spec.name", value = "ListenerExceptionHandlerSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("exception-handler")
        void go(@MessageBody String data)

        @Subject("exception-handler-default")
        void goDefault(@MessageBody String data)
    }

    @Requires(property = "spec.name", value = "ListenerExceptionHandlerSpec")
    @NatsListener
    static class HandlingConsumer implements NatsListenerExceptionHandler {

        List<NatsListenerException> errors = new CopyOnWriteArrayList<>()
        List<String> uris = new CopyOnWriteArrayList<>()

        @Subject("exception-handler")
        void listen(String data) {
            throw new IllegalStateException("boom " + data)
        }

        @Subject("exception-handler-uri")
        URI uri(String data) {
            uris.add(data)
            return URI.create("nats://" + data)
        }

        @Subject("exception-handler-header")
        void header(String data, @MessageHeader("required-header") String header) {
        }

        @Subject("exception-handler-reply")
        String reply(@Nullable String data) {
            return data ? data.toUpperCase() : null
        }

        @Override
        void handle(NatsListenerException exception) {
            errors.add(exception)
        }
    }

    @Requires(property = "spec.name", value = "ListenerExceptionHandlerSpec")
    @NatsListener
    static class NonHandlingConsumer {

        @Subject("exception-handler-default")
        void listen(String data) {
            throw new IllegalStateException("boom " + data)
        }
    }

    @Requires(property = "spec.name", value = "ListenerExceptionHandlerSpec")
    @Replaces(DefaultNatsListenerExceptionHandler)
    @Primary
    @Singleton
    static class RecordingExceptionHandler implements NatsListenerExceptionHandler {

        List<NatsListenerException> errors = new CopyOnWriteArrayList<>()

        @Override
        void handle(NatsListenerException exception) {
            errors.add(exception)
        }
    }
}
