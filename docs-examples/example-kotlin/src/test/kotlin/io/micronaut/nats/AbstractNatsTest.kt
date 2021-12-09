package io.micronaut.nats

import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy

abstract class AbstractNatsTest(body: BehaviorSpec.() -> Unit) : BehaviorSpec(body) {

    companion object {
        val natsContainer = KGenericContainer("nats:latest")
            .withExposedPorts(4222)
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
                "nats.addresses" to "nats://localhost:" + natsContainer.getMappedPort(4222),
                "spec.name" to specName
            )
    }

}
