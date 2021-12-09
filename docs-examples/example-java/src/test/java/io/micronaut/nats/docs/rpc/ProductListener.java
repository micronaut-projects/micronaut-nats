package io.micronaut.nats.docs.rpc;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "RpcUppercaseSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    @Subject("product")
    public String toUpperCase(String data) { // <1>
        return data.toUpperCase(); // <2>
    }
}
// end::clazz[]
