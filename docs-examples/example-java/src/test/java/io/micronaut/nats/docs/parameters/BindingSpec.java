package io.micronaut.nats.docs.parameters;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;

class BindingSpec extends AbstractNatsTest {

    @Test
    void testDynamicBinding() {
        startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("message body".getBytes());
        productClient.send("product", "message body2".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        waitFor(() ->
                productListener.messageLengths.size() == 2 &&
                productListener.messageLengths.contains(12) &&
                productListener.messageLengths.contains(13)
        );
    }
}
