package io.micronaut.nats.docs.consumer.custom.annotation;

import io.micronaut.context.annotation.Requires;
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.Subject;

@Requires(property = "spec.name", value = "SIDSpec")
@NatsClient
public interface ProductClient {

    @Subject("product")
    void send(byte[] data);
}
