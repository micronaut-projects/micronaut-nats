package io.micronaut.nats.docs.consumer.custom.type

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    CopyOnWriteArrayList<ProductInfo> messages = []

    @Subject("product")
    void receive(byte[] data,
                 ProductInfo productInfo) { // <1>
        messages << productInfo
    }
}
// end::clazz[]
