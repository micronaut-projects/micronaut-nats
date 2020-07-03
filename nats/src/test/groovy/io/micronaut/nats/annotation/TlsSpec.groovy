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
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

/**
 *
 * @author jgrimm
 */
class TlsSpec extends AbstractNatsTest {

    static GenericContainer natsContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .withClasspathResourceMapping("nats.crt", "/nats.crt", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("nats.key", "/nats.key", BindMode.READ_ONLY)
                    .withCommand("--tls", "--tlscert", "/nats.crt", "--tlskey", "/nats.key")
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    static {
        natsContainer.start()
    }

    protected ApplicationContext startContext(Map additionalConfig = [:]) {
        ApplicationContext.run(
                ["nats.addresses": ["nats://localhost:${natsContainer.getMappedPort(4222)}"],
                 "spec.name"     : getClass().simpleName] << additionalConfig, "test")
    }


    @Unroll
    void "test simple producing and consuming"() {
        ApplicationContext context = startContext(properties)
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
        producer.go("abc".bytes)
        producer.go("def".bytes)
//        boolean success = producer.goConfirm("def".bytes).blockingAwait(2, TimeUnit.SECONDS)

        then:
        conditions.eventually {
            consumer.messages.size() == messageCount
        }

        cleanup:
        context.close()

        where:
        properties                                                                                                                                                        | messageCount
        ["nats.tls.trust-store-path": "src/test/resources/nats.jks", "nats.tls.trust-store-password": 123456, "nats.tls.certificate-path": "src/test/resources/nats.crt"] | 2
        ["nats.tls.trust-store-path": "src/test/resources/nats.jks", "nats.tls.trust-store-password": 123456]                                                             | 4
        ["nats.tls.certificate-path": "src/test/resources/nats.crt"]                                                                                                      | 6

    }

    @Requires(property = "spec.name", value = "TlsSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("abc")
        void go(@Body byte[] data)

    }

    @Requires(property = "spec.name", value = "TlsSpec")
    @NatsListener
    static class MyConsumer {

        public static List<byte[]> messages = []

        @Subject("abc")
        void listen(byte[] data) {
            messages.add(data)
        }
    }
}
