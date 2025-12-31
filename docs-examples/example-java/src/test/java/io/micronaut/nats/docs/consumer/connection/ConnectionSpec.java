package io.micronaut.nats.docs.consumer.connection;

import io.micronaut.context.annotation.Property;
import io.micronaut.nats.testcontainers.Nats;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = "spec.name", value = "ConnectionSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        Map<String, String> containerProps = Nats.getProperties();
        String natsAddress = containerProps.get("nats.addresses");
        return Map.of(
            "nats.product-cluster.addresses", natsAddress
        );
    }
}
