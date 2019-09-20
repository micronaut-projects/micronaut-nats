package com.github.grimmjo.micronaut.nats.connect


import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.nats.client.Connection
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Shared
import spock.lang.Specification
/**
 *
 * @author jgrimm
 */
class SingleNatsConnectionFactoryConfigSpec extends Specification {

    @Shared
    GenericContainer natsContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    void "default nats configuration"() {
        given:
        natsContainer.start()
        String address = "nats://localhost:${natsContainer.getMappedPort(4222)}"
//        ApplicationContext context = startContext()
        ApplicationContext context = ApplicationContext.run(
                ["spec.name"   : getClass().simpleName,
                 "nats.address": address])

        expect:
        context.getBean(SingleNatsConnectionFactoryConfig, Qualifiers.byName(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)).address.get() == address
        context.getBean(Connection, Qualifiers.byName(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)).getServers().contains(address)
        context.getBean(Connection, Qualifiers.byName(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)).getConnectedUrl() == address

        cleanup:
        context.stop()
        natsContainer.stop()

    }
}
