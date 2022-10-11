package io.micronaut.nats.docs.jetstream

import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
// end::imports[]

import java.util.concurrent.CopyOnWriteArrayList



@Requires(property = "spec.name", value = "JetstreamSpec")
// tag::clazz[]
@JetStreamListener // <1>
class ProductListener {

    CopyOnWriteArrayList<byte[]> messageLengths = []

    @PushConsumer(value = "events", subject = "events.>", durable = "test") // <2>
    void receive(byte[] data) {
        messageLengths << data
    }
}
// end::clazz[]
