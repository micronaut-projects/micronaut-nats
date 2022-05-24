package io.micronaut.nats.docs.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    List<Integer> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @Subject("product") // <1>
    public void receive(byte[] data) {
        messageLengths.add(data.length);
    }
}
// end::clazz[]
