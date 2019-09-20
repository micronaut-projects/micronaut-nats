package com.github.grimmjo.micronaut.nats

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Specification

/**
 *
 * @author jgrimm
 */
abstract class AbstractNatsTest extends Specification {

    static GenericContainer natsContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    static {
        natsContainer.start()
    }

    protected ApplicationContext startContext(Map additionalConfig = [:]) {
        ApplicationContext.run(
                ["nats.address": "nats://localhost:${natsContainer.getMappedPort(4222)}",
                 "spec.name": getClass().simpleName] << additionalConfig, "test")
    }
}
