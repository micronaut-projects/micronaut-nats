package io.micronaut.nats.docs.rpc;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient;
import io.micronaut.nats.annotation.Subject;
import org.reactivestreams.Publisher;
// end::imports[]

@Requires(property = "spec.name", value = "RpcUppercaseSpec")
// tag::clazz[]
@NatsClient
public interface ProductClient {

    @Subject("product")
    String send(String data); // <1>

    @Subject("product")
    Publisher<String> sendReactive(String data); // <2>
}
// end::clazz[]
