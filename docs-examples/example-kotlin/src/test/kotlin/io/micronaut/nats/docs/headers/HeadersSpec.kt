package io.micronaut.nats.docs.headers

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.nats.client.impl.Headers
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "HeadersSpec")
class HeadersSpec(productClient: ProductClient, productListener: ProductListener) : BehaviorSpec({

    given("A basic producer and consumer") {
        `when`("The messages are published") {

            // tag::producer[]
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
                eventually(10.seconds) {
                    productListener.messageProperties.size shouldBe 6
                    productListener.messageProperties shouldContain "true|10|small"
                    productListener.messageProperties shouldContain "true|20|medium"
                    productListener.messageProperties shouldContain "true|30|null"
                    productListener.messageProperties shouldContain "true|40|large"
                    productListener.messageProperties shouldContain "true|20|xtra-small"
                    productListener.messageProperties shouldContain "true|20|xtra-large"
                }
            }
        }

    }
})
