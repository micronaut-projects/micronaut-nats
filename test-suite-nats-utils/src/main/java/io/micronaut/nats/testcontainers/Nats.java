package io.micronaut.nats.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class Nats {

    private static final String IMAGE_NAME = "nats:2.9";
    private static final int NATS_PORT = 4222;
    private static GenericContainer<?> container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withExposedPorts(NATS_PORT)
                .withCommand("--js")
                .waitingFor(Wait.forListeningPort());
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!container.isRunning());
            return getProperties(container);
        } else {
            return getProperties(container);
        }
    }

    private static Map<String, String> getProperties(GenericContainer<?> container) {
        int mappedPort = container.getMappedPort(NATS_PORT);
        String address = "nats://localhost:" + container.getMappedPort(NATS_PORT);
        return Map.of(
            "nats.addresses", address,
            "nats.port", String.valueOf(mappedPort)
        );
    }
}
