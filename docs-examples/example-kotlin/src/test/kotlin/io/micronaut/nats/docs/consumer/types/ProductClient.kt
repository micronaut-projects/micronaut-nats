package io.micronaut.nats.docs.consumer.types

import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageHeader
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "TypeBindingSpec")
// tag::clazz[]
@NatsClient // <1>
interface ProductClient {

    @Subject("product") // <2>
    fun send(data: ByteArray, @MessageHeader("x-product-count") count:Long) // <3>

}
// end::clazz[]
