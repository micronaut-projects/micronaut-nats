package io.micronaut.nats.docs.rpc

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import reactor.core.publisher.Mono

@MicronautTest
@Property(name = "spec.name", value = "RpcUppercaseSpec")
class RpcUppercaseSpec(productClient: ProductClient) : BehaviorSpec({

    given("A basic producer and consumer") {
        `when`("the message is published") {
            then("the message is consumed") {
                productClient.send("hello") shouldBe "HELLO"
                Mono.from(productClient.sendReactive("world")).block() shouldBe "WORLD"
            }
        }
    }
})
