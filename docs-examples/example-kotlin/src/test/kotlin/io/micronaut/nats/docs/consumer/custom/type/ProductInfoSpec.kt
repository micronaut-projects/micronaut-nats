package io.micronaut.nats.docs.consumer.custom.type

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "ProductInfoSpec")
class ProductInfoSpec : BehaviorSpec(), TestPropertyProvider {

    @Inject
    lateinit var productClient: ProductClient

    @Inject
    lateinit var productListener: ProductListener

    init {
        given("A custom type binder") {
            `when`("The messages are published") {

                // tag::producer[]
                productClient.send("body".toByteArray())
                productClient.send("medium", 20L, "body2".toByteArray())
                productClient.send(null, 30L, "body3".toByteArray())
                // end::producer[]

                then("The messages are received") {
                    eventually(10.seconds) {
                        productListener.messages.size shouldBe 3
                        productListener.messages shouldExist { p -> p.count == 10L && p.sealed }
                        productListener.messages shouldExist { p -> p.count == 20L && p.sealed }
                        productListener.messages shouldExist { p -> p.count == 30L && p.sealed }
                    }
                }
            }
        }
    }

    override fun getProperties(): Map<String, String> = Nats.getProperties()
}
