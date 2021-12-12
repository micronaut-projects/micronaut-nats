package io.micronaut.nats.docs.consumer.types

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject

import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Subscription
import io.nats.client.impl.Headers

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

@Requires(property = "spec.name", value = "TypeBindingSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    CopyOnWriteArrayList<String> messages = []

    @Subject("product")
    void receive(byte[] data,
                 Message message,
                 Connection connection,
                 Subscription subscription,
                 Headers headers) { // <1>
        def count = headers.get("x-product-count").get(0)
        messages << "subject: [$message.subject], maxPayload: [$connection.maxPayload], pendingMessageCount: [$subscription.pendingMessageCount], x-productCount: [$count]".toString()

    }
}
// end::clazz[]
