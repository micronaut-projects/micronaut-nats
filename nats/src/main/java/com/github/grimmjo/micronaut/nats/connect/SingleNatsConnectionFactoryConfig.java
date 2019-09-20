package com.github.grimmjo.micronaut.nats.connect;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Named;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.nats.client.Options;

/**
 * The default Nats configuration class.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@ConfigurationProperties("nats")
@Named(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)
public class SingleNatsConnectionFactoryConfig extends NatsConnectionFactoryConfig {

    public static final String DEFAULT_NAME = "default";

    private static final String DEFAULT_URL = Options.DEFAULT_URL;

    private String address = DEFAULT_URL;

    /**
     * Default constructor.
     */
    public SingleNatsConnectionFactoryConfig() {super(DEFAULT_NAME);}

    /**
     * @return the address
     */
    public Optional<String> getAddress() {
        return Optional.ofNullable(address);
    }

    /**
     * sets the address
     * @param address {@link String}
     */
    public void setAddress(@Nullable String address) {
        this.address = address;
    }
}
