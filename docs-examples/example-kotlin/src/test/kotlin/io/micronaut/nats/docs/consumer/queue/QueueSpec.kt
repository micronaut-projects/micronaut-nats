package io.micronaut.nats.docs.consumer.queue

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "QueueSpec")
class QueueSpec(productClient: ProductClient, productListener: ProductListener) : BehaviorSpec({

    given("A basic producer and consumer") {

        `when`("the message is published") {
// tag::producer[]
            productClient.send("queue-test".toByteArray())
// end::producer[]

            then("the message is consumed") {
                eventually(10.seconds) {
                    productListener.messageLengths.size shouldBe 1
                    productListener.messageLengths[0] shouldBe "queue-test"
                }
            }
        }
    }
})
