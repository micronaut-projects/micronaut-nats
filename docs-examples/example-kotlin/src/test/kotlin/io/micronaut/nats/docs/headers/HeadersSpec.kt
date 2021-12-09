package io.micronaut.nats.docs.headers

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import io.nats.client.impl.Headers
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class HeadersSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("body".toByteArray());
            productClient.send("medium", 20L, "body2".toByteArray());
            productClient.send(null, 30L, "body3".toByteArray());

            val headers = Headers()
            headers.put("productSize", "large")
            headers.put("x-product-count", "40")
            productClient.send("body4".toByteArray(), headers);
            productClient.send("body5".toByteArray(), listOf("xtra-small", "xtra-large"));
            // end::producer[]

            then("The messages are received") {
                eventually(Duration.seconds(10)) {
                    productListener.messageProperties.size shouldBe 6
                    productListener.messageProperties shouldContain "true|10|small"
                    productListener.messageProperties shouldContain "true|20|medium"
                    productListener.messageProperties shouldContain "true|30|medium"
                    productListener.messageProperties shouldContain "true|40|large"
                    productListener.messageProperties shouldContain "true|20|xtra-small"
                    productListener.messageProperties shouldContain "true|20|xtra-large"
                }
            }
        }

        ctx.stop()
    }
})
