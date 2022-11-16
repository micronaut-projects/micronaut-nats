package io.micronaut.nats.docs.consumer.types

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Subscription
import io.nats.client.impl.Headers
import java.util.Collections

// end::imports[]

@Requires(property = "spec.name", value = "TypeBindingSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    var messages: MutableList<String> = Collections.synchronizedList(ArrayList())
    private var datas: MutableList<ByteArray> = Collections.synchronizedList(ArrayList())

    @Subject(value = "product")
    fun receive(message: Message,
                connection: Connection,
                subscription: Subscription,
                headers: Headers) { // <1>
        messages.add("subject: [${message.subject}], maxPayload: [${connection.maxPayload}], pendingMessageCount: [${subscription.pendingMessageCount}], x-productCount: [${headers["x-product-count"][0]}]")
    }

}
// end::clazz[]
