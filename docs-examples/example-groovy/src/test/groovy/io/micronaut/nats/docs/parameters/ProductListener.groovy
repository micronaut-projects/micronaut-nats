package io.micronaut.nats.docs.parameters

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    CopyOnWriteArrayList<Integer> messageLengths = []

    @Subject("product") // <1>
    void receive(byte[] data) {
        messageLengths << data.length
    }
}
// end::clazz[]
