package io.micronaut.nats.docs.headers

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.core.annotation.Nullable
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.impl.Headers

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "HeadersSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    CopyOnWriteArrayList<String> messageProperties = []

    @Subject("product")
    void receive(byte[] data,
                 @MessageHeader("x-product-sealed") Boolean sealed, // <1>
                 @MessageHeader("x-product-count") Long count, // <2>
                 @Nullable @MessageHeader String productSize) { // <3>
        messageProperties << sealed.toString() + "|" + count + "|" + productSize
    }

    @Subject("products")
    void receive(@MessageBody byte[] data, @MessageHeader("x-product-sealed") Boolean sealed,
                 @MessageHeader("x-product-count") Long count, @MessageHeader List<String> productSizes) { // <4>
        productSizes.forEach {
            messageProperties << sealed.toString() + "|" + count + "|" + it
        }
    }

    @Subject("productHeader")
    void receive(@MessageBody byte[] data, Headers headers) { // <5>
        String productSize = headers.get("productSize").get(0)
        messageProperties << headers.get("x-product-sealed").get(0) + "|" +
                        headers.get("x-product-count").get(0) + "|" +
                        productSize
    }
}
// end::clazz[]
