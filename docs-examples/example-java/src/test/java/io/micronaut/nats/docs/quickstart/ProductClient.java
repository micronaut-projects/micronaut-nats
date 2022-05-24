package io.micronaut.nats.docs.quickstart;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.Subject;
import io.micronaut.nats.annotation.NatsClient;
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@NatsClient // <1>
public interface ProductClient {

    @Subject("product") // <2>
    void send(byte[] data); // <3>
}
// end::clazz[]
