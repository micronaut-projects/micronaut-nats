package io.micronaut.nats.docs.consumer.custom.annotation;

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
@Property(name = "spec.name", value = "SIDSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SIDSpec implements TestPropertyProvider {

    @Test
    void testUsingACustomAnnotationBinder(ProductClient productClient, ProductListener productListener) {
// tag::producer[]
        productClient.send("body".getBytes());
        productClient.send("body2".getBytes());
        productClient.send("body3".getBytes());
// end::producer[]

        await().atMost(60, SECONDS).until(() -> productListener.messages.size() == 3);
    }

    @Override
    public Map<String, String> getProperties() {
        return Nats.getProperties();
    }
}
