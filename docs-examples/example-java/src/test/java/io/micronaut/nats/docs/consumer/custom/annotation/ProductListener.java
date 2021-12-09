package io.micronaut.nats.docs.consumer.custom.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "SIDSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    List<String> messages = Collections.synchronizedList(new ArrayList<>());

    @Subject("product")
    public void receive(byte[] data, @SID String sid) { // <1>
        messages.add(sid);
    }
}
// end::clazz[]
