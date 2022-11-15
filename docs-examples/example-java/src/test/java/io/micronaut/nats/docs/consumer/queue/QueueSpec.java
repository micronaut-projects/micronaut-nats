package io.micronaut.nats.docs.consumer.queue;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;

class QueueSpec extends AbstractNatsTest {

    @Test
    void testProductClientAndListener() {
        startContext();

// tag::producer[]
ProductClient productClient = applicationContext.getBean(ProductClient.class);
productClient.send("queue-test".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        waitFor(() ->
                productListener.messageLengths.size() == 1 &&
                productListener.messageLengths.get(0).equals("queue-test")
        );
    }

}
