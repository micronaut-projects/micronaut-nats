package io.micronaut.nats.docs.serdes

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@NatsClient
interface ProductClient {

    @Subject("product")
    void send(@MessageBody ProductInfo data)
}
// end::clazz[]
