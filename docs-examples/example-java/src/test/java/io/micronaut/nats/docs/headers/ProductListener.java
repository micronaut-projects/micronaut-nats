package io.micronaut.nats.docs.headers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
// tag::imports[]
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
import io.nats.client.impl.Headers;
// end::imports[]

@Requires(property = "spec.name", value = "HeadersSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    Set<String> messageProperties = Collections.synchronizedSet(new HashSet<>());

    @Subject("product")
    public void receive(byte[] data,
            @MessageHeader("x-product-sealed") Boolean sealed, // <1>
            @MessageHeader("x-product-count") Long count, // <2>
            @Nullable @MessageHeader String productSize) { // <3>
        messageProperties.add(sealed + "|" + count + "|" + productSize);
    }

    @Subject("products")
    public void receive(@MessageBody byte[] data, @MessageHeader("x-product-sealed") Boolean sealed,
            @MessageHeader("x-product-count") Long count, @MessageHeader List<String> productSizes) { // <4>
        for (String productSize : productSizes) {
            messageProperties.add(sealed + "|" + count + "|" + productSize);
        }
    }

    @Subject("productHeader")
    public void receive(@MessageBody byte[] data, Headers headers) { // <5>
        String productSize = headers.get("productSize").get(0);
        messageProperties.add(
                headers.get("x-product-sealed").get(0) + "|" +
                        headers.get("x-product-count").get(0) + "|" +
                        productSize);
    }
}
// end::clazz[]
