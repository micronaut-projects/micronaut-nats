package io.micronaut.nats.docs.consumer.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "ConnectionSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    List<String> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @Subject(value = "product", connection = "product-cluster") // <1>
    public void receive(byte[] data) {
        messageLengths.add(new String(data));
        System.out.println("Java received " + data.length + " bytes from Nats");
    }
}
// end::clazz[]
