package io.micronaut.nats.docs.consumer.connection

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "ConnectionSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    List<String> messageLengths = Collections.synchronizedList([])

    @Subject(value = "product", connection = "product-cluster") // <1>
    void receive(byte[] data) {
        messageLengths << new String(data)
    }
}
// end::clazz[]
