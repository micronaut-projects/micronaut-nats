package io.micronaut.nats.docs.rpc

import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import reactor.core.publisher.Mono
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RpcUppercaseSpec: AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)

        `when`("the message is published") {

            val productClient = ctx.getBean(ProductClient::class.java)

            then("the message is consumed") {
                productClient.send("hello") shouldBe "HELLO"
                Mono.from(productClient.sendReactive("world")).block() shouldBe "WORLD"
            }
        }

        ctx.stop()
    }
})
