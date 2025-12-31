package io.micronaut.nats.docs.jetstream.kv;

import io.micronaut.context.annotation.Property;
import io.micronaut.nats.testcontainers.Nats;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.nats.client.JetStreamApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.Map;

@MicronautTest
@Property(name = "spec.name", value = "KeyValueTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeyValueTest implements TestPropertyProvider {

    @Test
    void simpleTest(KeyValueStoreHolder keyValueStoreHolder) throws JetStreamApiException, IOException {
        keyValueStoreHolder.put("test", "myvalue");

        Assertions.assertEquals("myvalue", keyValueStoreHolder.store.get("test").getValueAsString());
    }

    @Override
    public Map<String, String> getProperties() {
        return Nats.getProperties();
    }
}
