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
        ["nats.addresses": "nats://localhost:" + natsContainer.getMappedPort(4222),
         "spec.name": getClass().simpleName] as Map
    }

    protected void waitFor(Closure<?> conditionEvaluator) {
        conditions.eventually conditionEvaluator
    }

    void cleanup() {
        applicationContext?.close()
    }
}
