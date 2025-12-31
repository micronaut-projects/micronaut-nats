package io.micronaut.nats.docs.consumer.types;

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
@Property(name = "spec.name", value = "TypeBindingSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TypeBindingSpec implements TestPropertyProvider {

    @Test
    void testBindingByType(ProductClient productClient, ProductListener productListener) {

// tag::producer[]
        productClient.send("body".getBytes(), 20L);
        productClient.send("body2".getBytes(), 30L);
        productClient.send("body3".getBytes(), 40L);
// end::producer[]

        await().atMost(60, SECONDS).until(() ->
                productListener.messages.size() == 3 &&
                productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]") &&
                productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]") &&
                productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]")
        );
    }

    @Override
    public Map<String, String> getProperties() {
        return Nats.getProperties();
    }
}
