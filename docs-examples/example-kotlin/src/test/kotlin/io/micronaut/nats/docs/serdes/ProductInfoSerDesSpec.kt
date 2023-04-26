package io.micronaut.nats.docs.serdes

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "ProductInfoSerDesSpec")
class ProductInfoSerDesSpec(productClient: ProductClient, listener: ProductListener) : BehaviorSpec({

    given("A basic producer and consumer") {

        `when`("the message is published") {
// tag::producer[]
            productClient.send(ProductInfo("small", 10L, true))
            productClient.send(ProductInfo("medium", 20L, true))
            productClient.send(ProductInfo(null, 30L, false))
// end::producer[]

            then("the message is consumed") {
                eventually(10.seconds) {
                    listener.messages.size shouldBe 3
                    listener.messages shouldExist { p -> p.size == "small" && p.count == 10L && p.sealed }
                    listener.messages shouldExist { p -> p.size == "medium" && p.count == 20L && p.sealed }
                    listener.messages shouldExist { p -> p.size == null && p.count == 30L && !p.sealed }
                }
            }
        }
    }
})
