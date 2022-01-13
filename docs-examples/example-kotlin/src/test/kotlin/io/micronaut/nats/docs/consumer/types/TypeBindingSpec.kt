package io.micronaut.nats.docs.consumer.types

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TypeBindingSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)

        `when`("the message is published") {
            val productListener = ctx.getBean(ProductListener::class.java)

// tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("body".toByteArray(), 20L);
            productClient.send("body2".toByteArray(), 30L);
            productClient.send("body3".toByteArray(), 40L)
// end::producer[]

            then("the message is consumed") {
                eventually(Duration.seconds(10)) {
                    productListener.messages.size shouldBe 3
                    productListener.messages shouldContain "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]"
                    productListener.messages shouldContain "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]"
                    productListener.messages shouldContain "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]"
                }
            }
        }

        Thread.sleep(1000)
        ctx.stop()
    }
})
