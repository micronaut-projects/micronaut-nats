package io.micronaut.nats.docs.jetstream;

import io.micronaut.context.annotation.Requires;
import io.micronaut.nats.jetstream.annotation.JetStreamListener;
import io.micronaut.nats.jetstream.annotation.PushConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// end::imports[]

@Requires(property = "spec.name", value = "JetstreamTest")
// tag::clazz[]
@JetStreamListener // <1>
public class ProductListener {

    List<byte[]> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @PushConsumer(value = "myevents", subject = "myevents.>", durable = "test") // <2>
    public void receive(byte[] data) {
        messageLengths.add(data);
    }
}
// end::clazz[]
