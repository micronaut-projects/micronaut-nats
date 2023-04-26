package io.micronaut.nats.docs.rpc

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.core.publisher.Mono
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "RpcUppercaseSpec")
class RpcUppercaseSpec extends Specification {
    @Inject ProductClient productClient

    void "test product client and listener"() {
        when:
        // tag::producer[]
        productClient.send("hello") == "HELLO"

        then:

        Mono.from(productClient.sendReactive("world")).block() == "WORLD"
        // end::producer[]
    }
}
