package io.micronaut.nats.docs.consumer.connection

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "ConnectionSpec")
// tag::clazz[]
@NatsClient
interface ProductClient {

    @Subject(value = "product", connection = "product-cluster") // <1>
    fun send(data: ByteArray)

}
// end::clazz[]
