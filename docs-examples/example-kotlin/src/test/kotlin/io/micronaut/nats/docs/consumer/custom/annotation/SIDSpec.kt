package io.micronaut.nats.docs.consumer.custom.annotation

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SIDSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A custom type binder") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("body".toByteArray())
            productClient.send("body2".toByteArray())
            productClient.send("body3".toByteArray())
            // end::producer[]

            then("The messages are received") {
                eventually(10.seconds) {
                    productListener.messages.size shouldBe 3
                }
            }
        }

        ctx.stop()
    }
})
