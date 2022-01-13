package io.micronaut.nats.docs.headers

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
import io.nats.client.impl.Headers
// end::imports[]

@Requires(property = "spec.name", value = "HeadersSpec")
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

    @Subject("products")
    @MessageHeader(name = "x-product-count", value = "20")
    void send(@MessageBody byte[] data, @MessageHeader List<String> productSizes) // <4>

    @Subject("productHeader")
    void send(@MessageBody byte[] data, Headers headers) // <5>
}
// end::clazz[]
