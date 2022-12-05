package io.micronaut.nats.docs.consumer.connection

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ConnectionSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val config = AbstractNatsTest.getDefaultConfig(specName)
        config["nats.product-cluster.addresses"] = config.remove("nats.default.addresses")!!

        val ctx = startContext(config)

        `when`("the message is published") {
            val productListener = ctx.getBean(ProductListener::class.java)

// tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("connection-test".toByteArray())
// end::producer[]

            then("the message is consumed") {
                eventually(10.seconds) {
                    productListener.messageLengths.size shouldBe 1
                    productListener.messageLengths[0] shouldBe "connection-test"
                }
            }
        }

        ctx.stop()
    }
})
