package io.micronaut.nats;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public abstract class AbstractNatsTest {

    protected static GenericContainer<?> natsContainer = new GenericContainer<>("nats:latest")
        .withExposedPorts(4222)
        .withCommand("--js")
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
        config.put("nats.default.addresses",
            "nats://localhost:" + natsContainer.getMappedPort(4222));
        config.put("spec.name", getClass().getSimpleName());
        config.put("nats.default.jetstream.streams.events.storage-type", "Memory");
        config.put("nats.default.jetstream.streams.events.subjects", List.of("events.>"));
        config.put("nats.default.jetstream.keyvalue.examplebucket.storage-type", "Memory");
        config.put("nats.default.jetstream.keyvalue.examplebucket.max-history-per-key", 5);
        config.put("nats.default.jetstream.objectstore.examplebucket.storage-type", "Memory");
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
