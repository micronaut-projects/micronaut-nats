package io.micronaut.nats.docs.consumer.types;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;

class TypeBindingSpec extends AbstractNatsTest {

    @Test
    void testBindingByType() {
        startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("body".getBytes(), 20L);
        productClient.send("body2".getBytes(), 30L);
        productClient.send("body3".getBytes(), 40L);
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        waitFor(() ->
                productListener.messages.size() == 3 &&
                productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]") &&
                productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]") &&
                productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]")
        );
    }
}
