package io.micronaut.nats.docs.headers

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.messaging.annotation.MessageHeaders
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
import io.nats.client.impl.Headers
// end::imports[]

@Requires(property = "spec.name", value = "HeadersSpec")
// tag::clazz[]
@NatsClient
@MessageHeaders(
    MessageHeader(name = "x-product-sealed", value = "true"), // <1>
    MessageHeader(name = "productSize", value = "large")
)
interface ProductClient {

    @Subject("product")
    @MessageHeaders(
        MessageHeader(name = "x-product-count", value = "10"), // <2>
        MessageHeader(name = "productSize", value = "small")
    )
    fun send(data: ByteArray)

    @Subject("product")
    fun send(@MessageHeader productSize: String?, // <3>
             @MessageHeader("x-product-count") count: Long,
             data: ByteArray)

    @Subject("products")
    @MessageHeader(name = "x-product-count", value = "20")
    fun send(@MessageBody data:ByteArray, @MessageHeader productSizes: List<String>) // <4>

    @Subject("productHeader")
    fun send(@MessageBody data: ByteArray, headers: Headers) // <5>

}
// end::clazz[]
