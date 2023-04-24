package io.micronaut.nats.docs.headers;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.nats.client.impl.Headers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = "spec.name", value = "HeadersSpec")
class HeadersSpec {

    @Test
    void testPublishingAndReceivingHeaders(ProductClient productClient, ProductListener productListener) {
// tag::producer[]
        productClient.send("body".getBytes());
        productClient.send("medium", 20L, "body2".getBytes());
        productClient.send(null, 30L, "body3".getBytes());

        Headers headers = new Headers();
        headers.put("productSize", "large");
        headers.put("x-product-count", "40");
        productClient.send("body4".getBytes(), headers);
        productClient.send("body5".getBytes(), Arrays.asList("xtra-small", "xtra-large"));
// end::producer[]

        await().atMost(60, SECONDS).until(() ->
                productListener.messageProperties.size() == 6 &&
                productListener.messageProperties.contains("true|10|small") &&
                productListener.messageProperties.contains("true|20|medium") &&
                productListener.messageProperties.contains("true|30|null") &&
                productListener.messageProperties.contains("true|40|large") &&
                productListener.messageProperties.contains("true|20|xtra-small") &&
                productListener.messageProperties.contains("true|20|xtra-large")
        );
    }
}
