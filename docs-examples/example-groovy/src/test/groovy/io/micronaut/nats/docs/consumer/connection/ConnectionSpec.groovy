package io.micronaut.nats.docs.consumer.connection

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.micronaut.testresources.client.TestResourcesClientFactory
import jakarta.inject.Inject
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

@MicronautTest
@Property(name = "spec.name", value = "ConnectionSpec")
class ConnectionSpec extends Specification implements TestPropertyProvider {
    @Inject ProductClient productClient
    @Inject ProductListener productListener

    void "test product client and listener"() {
        when:
// tag::producer[]
        productClient.send("connection-test".bytes)
// end::producer[]

        then:
        await().atMost(10, SECONDS).until {
            productListener.messageLengths.size() == 1
            productListener.messageLengths[0] == "connection-test"
        }

        cleanup:
        // Finding that the context is closing the channel before ack is sent
        sleep 200
    }

    @Override
    Map<String, String> getProperties() {
        var client = TestResourcesClientFactory.fromSystemProperties().get()
        var natsPort = client.resolve("nats.port", Map.of(), Map.of(
                "containers.nats.startup-timeout", "600s",
                "containers.nats.image-name", "nats:latest",
                "containers.nats.exposed-ports[0].nats.port", 4222,
                "containers.nats.exposed-ports", List.of(Map.of("nats.port", 4222)),
                "containers.nats.command", "--js",
                "containers.nats.wait-strategy.log.regex", ".*Server is ready.*"
        ))
        return natsPort
                .map(port -> Map.of("nats.product-cluster.addresses[0]", "nats://localhost:$port"))
                .orElse(Collections.emptyMap())
    }
}
