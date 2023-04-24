package io.micronaut.nats.docs.consumer.connection

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.micronaut.testresources.client.TestResourcesClientFactory
import jakarta.inject.Inject
import java.util.Map
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "ConnectionSpec")
class ConnectionSpec: TestPropertyProvider, AnnotationSpec() {
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

    override fun getProperties(): MutableMap<String, String> {
        val client = TestResourcesClientFactory.fromSystemProperties().get()
        val natsPort = client.resolve("nats.port", Map.of(), Map.of())
        return natsPort
            .map { port: String ->
                Map.of(
                    "nats.product-cluster.addresses.0", "nats://localhost:$port"
                )
            }
            .orElse(emptyMap())
    }
}
