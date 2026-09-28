package io.micronaut.docs.nats;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.nats.testcontainers.Nats;

import java.util.HashMap;

/**
 * Supplies the {@code nats.addresses} / {@code nats.port} properties of the shared NATS test container
 * to the Python tests run with the {@code nats} environment, like the {@code TestPropertyProvider} of
 * the Java, Kotlin and Groovy example tests does.
 * <p>
 * The configurer is written in Java because Micronaut Test calls {@code TestPropertyProvider} before
 * the application context, and with it the GraalPy runtime, exists, so a Python test class cannot
 * supply the container properties. It uses the {@link #configure(ApplicationContext)} callback because
 * the {@link io.micronaut.context.ApplicationContextBuilder} is configured before {@code @MicronautTest}
 * selects the environments, so the {@code nats} environment can only be checked on the built context.
 */
@ContextConfigurer
public class NatsTestConfigurer implements ApplicationContextConfigurer {

    public static final String NATS_ENVIRONMENT = "nats";

    @Override
    public void configure(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (environment.getActiveNames().contains(NATS_ENVIRONMENT)) {
            environment.addPropertySource(PropertySource.of(NATS_ENVIRONMENT, new HashMap<>(Nats.getProperties())));
        }
    }
}
