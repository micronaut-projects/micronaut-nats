package com.github.grimmjo.micronaut.nats.connect;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import io.micronaut.context.annotation.Parameter;

/**
 * Base class for nats to be configured
 *
 * @author jgrimm
 * @since 1.0.0
 */
public abstract class NatsConnectionFactoryConfig {

    private final String name;

    private List<String> addresses = null;

    /**
     * Default constructor.
     *
     * @param name The name of the configuration
     */
    public NatsConnectionFactoryConfig(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name qualifier
     */
    public String getName() {
        return name;
    }

    /**
     * @return An optional list of addresses
     */
    public Optional<List<String>> getAddresses() {
        return Optional.ofNullable(addresses);
    }

    /**
     * Sets the addresses to be passed to {@link io.nats.client.Options.Builder#servers(String[])}}.
     *
     * @param addresses The list of addresses
     */
    public void setAddresses(@Nullable List<String> addresses) {
        this.addresses = addresses;
    }


}
