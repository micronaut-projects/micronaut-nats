package io.micronaut.nats.docs.parameters

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@NatsClient
interface ProductClient {

    @Subject("product") // <1>
    fun send(data: ByteArray)

    fun send(@Subject subject: String, data:ByteArray) // <2>
}
// end::clazz[]
