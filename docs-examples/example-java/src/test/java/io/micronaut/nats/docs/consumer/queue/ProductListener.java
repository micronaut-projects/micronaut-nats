package io.micronaut.nats.docs.consumer.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "QueueSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    List<String> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @Subject(value = "product", queue = "product-queue") // <1>
    public void receiveByQueue1(byte[] data) {
        messageLengths.add(new String(data));
        System.out.println("Java received " + data.length + " bytes from Nats");
    }

    @Subject(value = "product", queue = "product-queue")
    public void receiveByQueue2(byte[] data) {
        messageLengths.add(new String(data));
        System.out.println("Java received " + data.length + " bytes from Nats");
    }
}
// end::clazz[]
