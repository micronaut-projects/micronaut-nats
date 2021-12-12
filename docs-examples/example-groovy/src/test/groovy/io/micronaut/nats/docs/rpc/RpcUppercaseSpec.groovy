package io.micronaut.nats.docs.rpc

import io.micronaut.nats.AbstractNatsTest
import reactor.core.publisher.Mono

class RpcUppercaseSpec extends AbstractNatsTest {

    void "test product client and listener"() {
        startContext()

        when:
// tag::producer[]
        def productClient = applicationContext.getBean(ProductClient)

        then:
        productClient.send("hello") == "HELLO"
        Mono.from(productClient.sendReactive("world")).block() == "WORLD"
// end::producer[]
    }
}
