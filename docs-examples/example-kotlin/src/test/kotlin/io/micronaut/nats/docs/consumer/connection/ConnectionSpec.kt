package io.micronaut.nats.docs.consumer.connection

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "ConnectionSpec")
class ConnectionSpec : TestPropertyProvider, AnnotationSpec() {
    @Inject
    lateinit var productClient: ProductClient

    @Inject
    lateinit var productListener: ProductListener

    @Test
    suspend fun testBasicProducerAndConsumer() {
// tag::producer[]
        productClient.send("connection-test".toByteArray())
// end::producer[]
        eventually(10.seconds) {
            productListener.messageLengths.size shouldBe 1
            productListener.messageLengths[0] shouldBe "connection-test"
        }
    }

    override fun getProperties(): Map<String, String> {
        val containerProps = Nats.getProperties()
        val natsAddress = containerProps["nats.addresses"] ?: ""

        return mapOf(
            "nats.product-cluster.addresses" to natsAddress
        )
    }
}
