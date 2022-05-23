package io.micronaut.nats.docs.headers

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.impl.Headers
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "HeadersSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    var messageProperties: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Subject("product")
    fun receive(data: ByteArray,
                @MessageHeader("x-product-sealed") sealed: Boolean, // <1>
                @MessageHeader("x-product-count") count: Long, // <2>
                @MessageHeader productSize: String?) { // <3>
        messageProperties.add(sealed.toString() + "|" + count + "|" + productSize)
    }

    @Subject("products")
    fun receive(@MessageBody data: ByteArray,
                @MessageHeader("x-product-sealed") sealed: Boolean,
                @MessageHeader("x-product-count") count: Long,
                @MessageHeader productSizes: List<String>?) { // <4>
        productSizes?.forEach {
            messageProperties.add("${sealed}|${count}|${it}")
        }
    }

    @Subject("productHeader")
    fun receive(@MessageBody data: ByteArray, headers: Headers) { // <5>
        messageProperties.add("${headers["x-product-sealed"][0]}|${headers["x-product-count"][0]}|${headers["productSize"][0]}")
    }
}
// end::clazz[]
