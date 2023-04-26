package io.micronaut.nats.docs.jetstream.kv;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.nats.client.JetStreamApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@MicronautTest
@Property(name = "spec.name", value = "KeyValueTest")
class KeyValueTest {

    @Test
    void simpleTest(KeyValueStoreHolder keyValueStoreHolder) throws JetStreamApiException, IOException {
        keyValueStoreHolder.put("test", "myvalue");

        Assertions.assertEquals("myvalue", keyValueStoreHolder.store.get("test").getValueAsString());
    }
}
