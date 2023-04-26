package io.micronaut.nats.docs.consumer.types

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "TypeBindingSpec")
class TypeBindingSpec(productClient: ProductClient, productListener: ProductListener) : BehaviorSpec({

    given("A basic producer and consumer") {
        `when`("the message is published") {
// tag::producer[]
            productClient.send("body".toByteArray(), 20L);
            productClient.send("body2".toByteArray(), 30L);
            productClient.send("body3".toByteArray(), 40L)
// end::producer[]

            then("the message is consumed") {
                eventually(10.seconds) {
                    productListener.messages.size shouldBe 3
                    productListener.messages shouldContain "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]"
                    productListener.messages shouldContain "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]"
                    productListener.messages shouldContain "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]"
                }
            }
        }
    }
})
