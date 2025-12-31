package io.micronaut.nats.docs.rpc

import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import reactor.core.publisher.Mono
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "RpcUppercaseSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RpcUppercaseSpec extends Specification implements TestPropertyProvider{
    @Inject ProductClient productClient

    void "test product client and listener"() {
        when:
        // tag::producer[]
        productClient.send("hello") == "HELLO"

        then:

        Mono.from(productClient.sendReactive("world")).block() == "WORLD"
        // end::producer[]
    }

    @Override
    Map<String, String> getProperties() {
        Nats.properties
    }
}
