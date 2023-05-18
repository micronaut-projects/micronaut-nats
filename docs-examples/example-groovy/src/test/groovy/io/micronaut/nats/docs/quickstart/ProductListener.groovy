package io.micronaut.nats.docs.quickstart

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@NatsListener // <1>
class ProductListener {

    CopyOnWriteArrayList<String> messageLengths = []

    @Subject("product") // <2>
    void receive(byte[] data) { // <3>
        messageLengths << new String(data)
    }
}
// end::clazz[]
