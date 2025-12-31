package io.micronaut.nats.docs.consumer.custom.annotation

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "SIDSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SIDSpec : BehaviorSpec(), TestPropertyProvider {
    @Inject
    lateinit var productClient: ProductClient

    @Inject
    lateinit var productListener: ProductListener
    init {
        given("A custom type binder") {
            `when`("The messages are published") {
                productClient.send("body".toByteArray())
                productClient.send("body2".toByteArray())
                productClient.send("body3".toByteArray())

                then("The messages are received") {
                    eventually(10.seconds) {
                        productListener.messages.size shouldBe 3
                    }
                }
            }
        }
    }

    override fun getProperties(): Map<String, String> = Nats.getProperties()
}
