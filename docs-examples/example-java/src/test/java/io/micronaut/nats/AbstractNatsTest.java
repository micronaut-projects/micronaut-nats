package io.micronaut.nats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public abstract class AbstractNatsTest {

    protected static GenericContainer<?> natsContainer = new GenericContainer<>("nats:latest")
            .withExposedPorts(4222)
            .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"));

    static {
        natsContainer.start();
    }

    protected ApplicationContext applicationContext;

    protected void startContext() {
        applicationContext = ApplicationContext.run(getConfiguration(), "test");
    }

    protected Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("nats.addresses", "nats://localhost:" + natsContainer.getMappedPort(4222));
        config.put("spec.name", getClass().getSimpleName());
        return config;
    }

    protected void waitFor(Callable<Boolean> conditionEvaluator) {
        await().atMost(5, SECONDS).until(conditionEvaluator);
    }

    @AfterEach
    void cleanup() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }
}
