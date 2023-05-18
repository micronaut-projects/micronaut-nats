package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "SIDSpec")
// tag::clazz[]
@NatsClient
interface ProductClient {

    @Subject("product")
    void send(byte[] data)
}
// end::clazz[]
