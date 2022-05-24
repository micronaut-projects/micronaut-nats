package io.micronaut.nats.docs.consumer.connection

import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageBody
// tag::imports[]
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.impl.Headers
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "ConnectionSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    var messageLengths: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Subject(value = "product", connection = "product-cluster") // <1>
    fun receive(data: ByteArray) {
        messageLengths.add(String(data))
    }

}
// end::clazz[]
