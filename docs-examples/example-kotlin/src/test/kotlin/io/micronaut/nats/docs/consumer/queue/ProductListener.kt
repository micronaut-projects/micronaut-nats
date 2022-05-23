package io.micronaut.nats.docs.consumer.queue

import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageBody
// tag::imports[]
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.impl.Headers
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "QueueSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    var messageLengths: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Subject(value = "product", queue = "product-queue") // <1>
    fun receiveByQueue1(data: ByteArray) {
        messageLengths.add(String(data))
    }

    @Subject(value = "product", queue = "product-queue")
    fun receiveByQueue2(data: ByteArray) {
        messageLengths.add(String(data))
    }

}
// end::clazz[]
