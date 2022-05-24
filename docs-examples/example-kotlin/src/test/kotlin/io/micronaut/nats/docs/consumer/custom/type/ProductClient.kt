package io.micronaut.nats.docs.consumer.custom.type

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.messaging.annotation.MessageHeaders
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@NatsClient // <1>
@MessageHeaders(
    MessageHeader(name = "x-product-sealed", value = "true"), // <1>
    MessageHeader(name = "productSize", value = "large")
)
interface ProductClient {

    @Subject("product") // <2>
    @MessageHeaders(
        MessageHeader(name = "x-product-count", value = "10"), // <2>
        MessageHeader(name = "productSize", value = "small")
    )
    fun send(data: ByteArray)

    @Subject("product")
    fun send(@MessageHeader productSize: String?,
             @MessageHeader("x-product-count") count: Long,
             data: ByteArray
    )


}
// end::clazz[]
