package io.micronaut.nats.docs.consumer.queue

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "QueueSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    CopyOnWriteArrayList<String> messageLengths = []

    @Subject(value = "product", queue = "product-queue") // <1>
    void receiveByQueue1(byte[] data) {
        messageLengths << new String(data)
    }

    @Subject(value = "product", queue = "product-queue")
    public void receiveByQueue2(byte[] data) {
        messageLengths << new String(data)
    }
}
// end::clazz[]
