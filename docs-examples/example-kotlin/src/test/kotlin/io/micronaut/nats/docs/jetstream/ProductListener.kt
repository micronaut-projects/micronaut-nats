package io.micronaut.nats.docs.jetstream


import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
// end::imports[]
import java.util.*


@Requires(property = "spec.name", value = "JetstreamSpec")
// tag::clazz[]
@JetStreamListener // <1>
class ProductListener {

    val messageLengths: MutableList<ByteArray> = Collections.synchronizedList(ArrayList())

    @PushConsumer(value = "events", subject = "events.>", durable = "test") // <2>
    fun receive(data: ByteArray) {
        messageLengths.add(data)
    }
}
// end::clazz[]
