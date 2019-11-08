package io.micronaut.nats.docs.quickstart;

// tag::imports[]
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.Subject;
import io.micronaut.messaging.annotation.Body;
// end::imports[]

// tag::clazz[]
@NatsClient // <1>
public interface ProductClient {

    @Subject("abc") // <2>
    void send(@Body byte[] data); // <3>

}
// end::clazz[]
