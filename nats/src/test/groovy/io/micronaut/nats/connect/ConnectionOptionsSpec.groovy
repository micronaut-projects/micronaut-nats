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
package io.micronaut.nats.connect

import io.micronaut.context.ApplicationContext
import io.nats.client.Options
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.time.Duration

class ConnectionOptionsSpec extends Specification {

    @TempDir
    Path tempDir

    void "further connection options are configurable"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                "nats.default.addresses"                           : ["nats://localhost:4222"],
                "nats.default.reconnect-wait"                      : "5s",
                "nats.default.max-pings-out"                       : 7,
                "nats.default.max-messages-in-outgoing-queue"      : 1000,
                "nats.default.discard-messages-when-outgoing-queue-full": true,
                "nats.default.no-randomize"                        : true,
                "nats.default.request-cleanup-interval"            : "10s",
                "nats.default.reconnect-jitter"                    : "200ms",
                "nats.default.socket-write-timeout"                : "30s",
                "nats.default.ignore-discovered-servers"           : true,
        ], "test")

        when:
        Options options = context.getBean(NatsConnectionFactoryConfig).toOptionsBuilder().build()

        then:
        options.reconnectWait == Duration.ofSeconds(5)
        options.maxPingsOut == 7
        options.maxMessagesInOutgoingQueue == 1000
        options.discardMessagesWhenOutgoingQueueFull
        options.noRandomize
        options.requestCleanupInterval == Duration.ofSeconds(10)
        options.reconnectJitter == Duration.ofMillis(200)
        options.socketWriteTimeout == Duration.ofSeconds(30)
        options.ignoreDiscoveredServers

        cleanup:
        context.close()
    }

    void "every call returns an independent builder"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                "nats.default.addresses": ["nats://localhost:4222", "nats://localhost:4223"],
        ], "test")
        NatsConnectionFactoryConfig config = context.getBean(NatsConnectionFactoryConfig)

        when:
        config.toOptionsBuilder().server("nats://other:4222").maxPingsOut(42)
        Options options = config.toOptionsBuilder().build()

        then:
        options.servers*.toString() == ["nats://localhost:4222", "nats://localhost:4223"]
        options.maxPingsOut == Options.DEFAULT_MAX_PINGS_OUT

        cleanup:
        context.close()
    }

    void "defaults are kept without further connection options"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                "nats.default.addresses": ["nats://localhost:4222"],
        ], "test")

        when:
        Options options = context.getBean(NatsConnectionFactoryConfig).toOptionsBuilder().build()

        then:
        options.maxPingsOut == Options.DEFAULT_MAX_PINGS_OUT
        !options.noRandomize
        options.connectionName == "default"

        cleanup:
        context.close()
    }

    void "mutual tls with a key store"() {
        given:
        Path keyStore = tempDir.resolve("client.p12")
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", "client", "-keyalg", "RSA", "-keysize", "2048",
                "-dname", "CN=client", "-validity", "1", "-storetype", "PKCS12",
                "-keystore", keyStore.toString(), "-storepass", "secret", "-keypass", "secret")
                .redirectErrorStream(true)
                .start()
        assert process.waitFor() == 0

        ApplicationContext context = ApplicationContext.run([
                "nats.default.addresses"              : ["tls://localhost:4222"],
                "nats.default.tls.key-store-path"     : keyStore.toString(),
                "nats.default.tls.key-store-password" : password,
                "nats.default.tls.key-store-type"     : "PKCS12",
        ], "test")

        when:
        Options options = context.getBean(NatsConnectionFactoryConfig).toOptionsBuilder().build()

        then:
        options.sslContext != null

        when:
        context.close()
        context = ApplicationContext.run([
                "nats.default.addresses"              : ["tls://localhost:4222"],
                "nats.default.tls.key-store-path"     : keyStore.toString(),
                "nats.default.tls.key-store-password" : "wrong",
        ], "test")
        context.getBean(NatsConnectionFactoryConfig).toOptionsBuilder()

        then:
        thrown(IOException)

        cleanup:
        context.close()

        where:
        password = "secret"
    }
}
