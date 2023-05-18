package io.micronaut.nats.docs.consumer.custom.type

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@NatsClient
@MessageHeader(name = "x-product-sealed", value = "true") // <1>
@MessageHeader(name = "productSize", value = "large")
interface ProductClient {

    @Subject("product")
    @MessageHeader(name = "x-product-count", value = "10") // <2>
    @MessageHeader(name = "productSize", value = "small")
    void send(byte[] data)

    @Subject("product")
    void send(@MessageHeader String productSize, // <3>
              @MessageHeader("x-product-count") Long count,
              byte[] data)
}
// end::clazz[]
