package io.micronaut.nats.docs.serdes

import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageBody
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.docs.serdes.ProductInfo
import org.reactivestreams.Publisher
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@NatsClient
interface ProductClient {

    @Subject("product")
    fun send(@MessageBody data: ProductInfo)

}
// end::clazz[]
