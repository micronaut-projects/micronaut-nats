package io.micronaut.nats.docs.parameters;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@NatsClient
public interface ProductClient {

    @Subject("product") // <1>
    void send(byte[] data);

    void send(@Subject String subject, byte[] data); // <2>
}
// end::clazz[]
