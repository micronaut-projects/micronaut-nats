package io.micronaut.nats

import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy

abstract class AbstractNatsTest(body: BehaviorSpec.() -> Unit) : BehaviorSpec(body) {

    companion object {
        val natsContainer = KGenericContainer("nats:latest")
            .withExposedPorts(4222)
            .withCommand("--js")
            .waitingFor(LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

        init {
            natsContainer.start()
        }

        fun startContext(specName: String): ApplicationContext =
            ApplicationContext.run(getDefaultConfig(specName), "test")

        fun startContext(configuration: Map<String, Any>): ApplicationContext =
            ApplicationContext.run(configuration, "test")

        fun getDefaultConfig(specName: String): MutableMap<String, Any> =
            mutableMapOf(
                "nats.default.addresses" to "nats://localhost:" + natsContainer.getMappedPort(4222),
                "spec.name" to specName,
                "nats.default.jetstream.streams.events.storage-type" to "Memory",
                "nats.default.jetstream.streams.events.subjects" to "events.>",
                "nats.default.jetstream.keyvalue.examplebucket.storage-type" to "Memory",
                "nats.default.jetstream.keyvalue.examplebucket.max-history-per-key" to 5,
                "nats.default.jetstream.objectstore.examplebucket.storage-type" to "Memory"
            )
    }

}
