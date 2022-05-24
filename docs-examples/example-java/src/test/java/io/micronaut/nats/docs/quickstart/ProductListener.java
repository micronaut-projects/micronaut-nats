package io.micronaut.nats.docs.quickstart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@NatsListener // <1>
public class ProductListener {

    List<String> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @Subject("product") // <2>
    public void receive(byte[] data) { // <3>
        messageLengths.add(new String(data));
        System.out.println("Java received " + data.length + " bytes from Nats");
    }
}
// end::clazz[]
