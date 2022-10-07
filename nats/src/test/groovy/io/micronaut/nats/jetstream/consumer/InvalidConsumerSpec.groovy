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
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.messaging.exceptions.MessageListenerException
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.jetstream.AbstractJetstreamTest
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
import spock.lang.Unroll

class InvalidConsumerSpec extends AbstractJetstreamTest {

    @Unroll
    void "invalid consumer #consumer"() {
        when:
        startContext(["consumer": consumer])

        then:
        thrown MessageListenerException

        where:
        consumer << ["invalidSubject", "emptySubject", "emptyStream"]
    }

    void "no @JetstreamListener annotation"() {
        when:
        ApplicationContext context = startContext(["consumer": "noListener"])

        then:
        context.getBeansOfType(Object.class, Qualifiers.byStereotype(JetStreamListener.class)).isEmpty()

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "InvalidConsumerSpec")
    @Requires(property = "consumer", value = "invalidSubject")
    @JetStreamListener
    static class InvalidSubjectConsumer {

        @PushConsumer("widgets")

        void listen(byte[] data) {
        }
    }

    @Requires(property = "spec.name", value = "InvalidConsumerSpec")
    @Requires(property = "consumer", value = "emptySubject")
    @JetStreamListener
    static class EmptySubjectConsumer {

        @PushConsumer("widgets")
        @Subject("")
        void listen(byte[] data) {
        }
    }

    @Requires(property = "spec.name", value = "InvalidConsumerSpec")
    @Requires(property = "consumer", value = "emptyStream")
    @JetStreamListener
    static class EmptyStreamConsumer {

        @PushConsumer("")
        @Subject("subject.>")
        void listen(byte[] data) {
        }
    }

    @Requires(property = "spec.name", value = "InvalidConsumerSpec")
    @Requires(property = "consumer", value = "noListener")
    static class NoListenerConsumer {

        @PushConsumer("widgets")
        @Subject("subject.>")
        void listen(byte[] data) {
        }
    }
}
