package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.docs.consumer.custom.type.ProductInfo

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "SIDSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    CopyOnWriteArrayList<ProductInfo> messages = []

    @Subject("product")
    void receive(byte[] data, @SID String sid) {
        messages << sid
    }
}
// end::clazz[]
