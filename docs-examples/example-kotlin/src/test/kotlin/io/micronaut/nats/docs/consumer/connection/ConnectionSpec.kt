package io.micronaut.nats.docs.consumer.connection

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.micronaut.testresources.client.TestResourcesClientFactory
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

    override fun getProperties(): MutableMap<String, String> {
        val client = TestResourcesClientFactory.fromSystemProperties().get()
        val natsPort = client.resolve(
            "nats.port",
            mapOf(),
            mapOf(
                "containers.nats.startup-timeout" to "600s",
                "containers.nats.image-name" to "nats:latest",
                "containers.nats.exposed-ports[0].nats.port" to 4222,
                "containers.nats.exposed-ports" to listOf(mapOf("nats.port" to 4222)),
                "containers.nats.command" to "--js",
                "containers.nats.wait-strategy.log.regex" to ".*Server is ready.*"
            )
        )
        return natsPort
            .map { port: String ->
                mutableMapOf(
                    "nats.product-cluster.addresses[0]" to "nats://localhost:$port"
                )
            }
            .orElse(mutableMapOf())
    }
}
