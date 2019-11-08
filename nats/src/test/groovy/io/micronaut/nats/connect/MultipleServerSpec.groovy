package io.micronaut.nats.connect

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.nats.client.Connection
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Shared
import spock.lang.Specification

class MultipleServerSpec extends Specification {

    @Shared
    GenericContainer natsContainer1 =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))
    @Shared
    GenericContainer natsContainer2 =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    void "test multiple server configuration"() {
        given:
        natsContainer1.start()
        natsContainer2.start()

        def natsAddressContainer1 = "nats://localhost:${natsContainer1.getMappedPort(4222)}".toString()
        def natsAddressContainer2 = "nats://localhost:${natsContainer2.getMappedPort(4222)}".toString()
        ApplicationContext context = ApplicationContext.run(
                ["spec.name"                : getClass().simpleName,
                 "nats.servers.one.addresses": [natsAddressContainer1],
                 "nats.servers.two.addresses": [natsAddressContainer2]
                ])

        expect:
        context.getBean(NatsConnectionFactoryConfig, Qualifiers.byName("one")).addresses.get().contains(natsAddressContainer1)
        context.getBean(Connection, Qualifiers.byName("one")).getServers().contains(natsAddressContainer1)
        context.getBean(Connection, Qualifiers.byName("one")).getConnectedUrl() == natsAddressContainer1
        context.getBean(NatsConnectionFactoryConfig, Qualifiers.byName("two")).addresses.get().contains(natsAddressContainer2)
        context.getBean(Connection, Qualifiers.byName("two")).getServers().contains(natsAddressContainer2)
        context.getBean(Connection, Qualifiers.byName("two")).getConnectedUrl() == natsAddressContainer2

        cleanup:
        context.stop()
        natsContainer1.stop()
        natsContainer2.stop()
    }

}
