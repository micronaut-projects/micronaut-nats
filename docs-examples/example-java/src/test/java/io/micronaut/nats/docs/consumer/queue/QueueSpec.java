package io.micronaut.nats.docs.consumer.queue;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = "spec.name", value = "QueueSpec")
class QueueSpec {

    @Test
    void testProductClientAndListener(ProductClient productClient, ProductListener productListener) {
        // tag::producer[]
        productClient.send("queue-test".getBytes());
        // end::producer[]

        await().atMost(60, SECONDS).until(() ->
            productListener.messageLengths.size() == 1 &&
                productListener.messageLengths.get(0).equals("queue-test")
        );
    }

}
