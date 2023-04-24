package io.micronaut.nats.docs.consumer.connection;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.testresources.client.TestResourcesClientFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = "spec.name", value = "ConnectionSpec")
class ConnectionSpec implements TestPropertyProvider {

    @Test
    void testProductClientAndListener(ProductClient productClient, ProductListener productListener) {

// tag::producer[]
productClient.send("connection-test".getBytes());
// end::producer[]

        await().atMost(10, SECONDS).until(() ->
                productListener.messageLengths.size() == 1 &&
                productListener.messageLengths.get(0).equals("connection-test")
        );
    }

    @Override
    public Map<String, String> getProperties() {
        var client = TestResourcesClientFactory.fromSystemProperties().get();
        var natsPort = client.resolve("nats.port", Map.of(), Map.of());
        return natsPort
            .map(port -> Map.of("nats.product-cluster.addresses.0", "nats://localhost:" + port))
            .orElse(Collections.emptyMap());
    }
}
