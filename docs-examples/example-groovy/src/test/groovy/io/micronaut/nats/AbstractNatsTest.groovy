package io.micronaut.nats

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

abstract class AbstractNatsTest extends Specification {

    static GenericContainer natsContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .withCommand("--js")
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    static {
        natsContainer.start()
    }

    protected ApplicationContext applicationContext
    protected PollingConditions conditions = new PollingConditions(timeout: 5)

    protected void startContext() {
        applicationContext = ApplicationContext.run(configuration, "test")
    }

    protected Map<String, Object> getConfiguration() {
        ["nats.default.addresses": "nats://localhost:" + natsContainer.getMappedPort(4222),
         "spec.name"             : getClass().simpleName,
         "nats.default.jetstream.streams.events.storage-type": "Memory",
         "nats.default.jetstream.streams.events.subjects": ["events.>"],
         "nats.default.jetstream.keyvalue.examplebucket.storage-type": "Memory",
         "nats.default.jetstream.keyvalue.examplebucket.max-history-per-key": 5
        ] as Map
    }

    protected void waitFor(Closure<?> conditionEvaluator) {
        conditions.eventually conditionEvaluator
    }

    void cleanup() {
        applicationContext?.close()
    }
}
