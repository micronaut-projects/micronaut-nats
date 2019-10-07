package com.github.grimmjo.micronaut.nats.docs.quickstart;

// tag::imports[]
import com.github.grimmjo.micronaut.nats.annotation.NatsClient;
import com.github.grimmjo.micronaut.nats.annotation.Subject;
import io.micronaut.messaging.annotation.Body;
// end::imports[]

// tag::clazz[]
@NatsClient // <1>
public interface ProductClient {

    @Subject("abc") // <2>
    void send(@Body byte[] data); // <3>

}
// end::clazz[]
