package io.micronaut.nats.docs.consumer.custom.annotation;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;

class SIDSpec extends AbstractNatsTest {

    @Test
    void testUsingACustomAnnotationBinder() {
        startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("body".getBytes());
        productClient.send("body2".getBytes());
        productClient.send("body3".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        waitFor(() -> productListener.messages.size() == 3);
    }
}
