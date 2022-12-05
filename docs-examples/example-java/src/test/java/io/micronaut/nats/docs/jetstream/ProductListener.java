package io.micronaut.nats.docs.jetstream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.JetStreamListener;
import io.micronaut.nats.jetstream.annotation.PushConsumer;
// end::imports[]

@Requires(property = "spec.name", value = "JetstreamTest")
// tag::clazz[]
@JetStreamListener // <1>
public class ProductListener {

    List<byte[]> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @PushConsumer(value = "events", subject = "events.>", durable = "test") // <2>
    public void receive(byte[] data) {
        messageLengths.add(data);
    }
}
// end::clazz[]
