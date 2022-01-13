package io.micronaut.nats.docs.consumer.connection

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ConnectionSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val config = AbstractNatsTest.getDefaultConfig(specName)
        config["nats.servers.product-cluster.addresses"] = config.remove("nats.addresses")!!

        val ctx = startContext(config)

        `when`("the message is published") {
            val productListener = ctx.getBean(ProductListener::class.java)

// tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("connection-test".toByteArray())
// end::producer[]

            then("the message is consumed") {
                eventually(Duration.seconds(10)) {
                    productListener.messageLengths.size shouldBe 1
                    productListener.messageLengths[0] shouldBe "connection-test"
                }
            }
        }

        ctx.stop()
    }
})
