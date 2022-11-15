package io.micronaut.nats.docs.consumer.connection;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ConnectionSpec extends AbstractNatsTest {

    @Test
    void testProductClientAndListener() {
        startContext();

// tag::producer[]
ProductClient productClient = applicationContext.getBean(ProductClient.class);
productClient.send("connection-test".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        waitFor(() ->
                productListener.messageLengths.size() == 1 &&
                productListener.messageLengths.get(0).equals("connection-test")
        );
    }

    @Override
    protected Map<String, Object> getConfiguration() {
        Map<String, Object> configuration = super.getConfiguration();
        configuration.put("nats.servers.product-cluster.addresses", configuration.remove("nats.addresses"));
        return configuration;
    }
}
