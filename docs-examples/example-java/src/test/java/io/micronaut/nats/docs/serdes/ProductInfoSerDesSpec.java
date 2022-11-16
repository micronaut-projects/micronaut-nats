package io.micronaut.nats.docs.serdes;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;

class ProductInfoSerDesSpec extends AbstractNatsTest {

    @Test
    void testUsingACustomSerDes() {
        startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send(new ProductInfo("small", 10L, true));
        productClient.send(new ProductInfo("medium", 20L, true));
        productClient.send(new ProductInfo(null, 30L, false));
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        waitFor(() ->
                productListener.messages.size() == 3 &&
                productListener.messages.stream().anyMatch(pi -> pi.getCount() == 10L) &&
                productListener.messages.stream().anyMatch(pi -> pi.getCount() == 20L) &&
                productListener.messages.stream().anyMatch(pi -> pi.getCount() == 30L)
        );
    }
}
