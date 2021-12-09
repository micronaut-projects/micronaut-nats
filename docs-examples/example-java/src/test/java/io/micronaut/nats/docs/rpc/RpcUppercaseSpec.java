package io.micronaut.nats.docs.rpc;

import io.micronaut.nats.AbstractNatsTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RpcUppercaseSpec extends AbstractNatsTest {

    @Test
    void testProductClientAndListener() {
        startContext();

// tag::producer[]
ProductClient productClient = applicationContext.getBean(ProductClient.class);
assertEquals("RPC", productClient.send("rpc"));
assertEquals("HELLO", Mono.from(productClient.sendReactive("hello")).block());
// end::producer[]
    }
}
