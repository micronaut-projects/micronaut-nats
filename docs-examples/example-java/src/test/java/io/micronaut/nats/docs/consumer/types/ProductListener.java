package io.micronaut.nats.docs.consumer.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Subscription;
import io.nats.client.impl.Headers;
// end::imports[]

@Requires(property = "spec.name", value = "TypeBindingSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    List<String> messages = Collections.synchronizedList(new ArrayList<>());

    @Subject("product")
    public void receive(byte[] data,
            Message message,
            Connection connection,
            Subscription subscription,
            Headers headers) { // <1>
        messages.add(String.format("subject: [%s], maxPayload: [%s], pendingMessageCount: [%s], x-productCount: [%s]",
                message.getSubject(),
                connection.getMaxPayload(), subscription.getPendingMessageCount(),
                headers.get("x-product-count").get(0)));
    }
}
// end::clazz[]
