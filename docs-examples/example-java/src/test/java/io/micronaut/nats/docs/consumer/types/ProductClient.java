package io.micronaut.nats.docs.consumer.types;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "TypeBindingSpec")
// tag::clazz[]
@NatsClient // <1>
public interface ProductClient {

    @Subject("product") // <2>
    void send(byte[] data, @MessageHeader("x-product-count") Long count); // <3>
}
// end::clazz[]
