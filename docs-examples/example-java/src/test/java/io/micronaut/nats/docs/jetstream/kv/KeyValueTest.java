package io.micronaut.nats.docs.jetstream.kv;

import io.micronaut.nats.AbstractNatsTest;
import io.nats.client.JetStreamApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class KeyValueTest extends AbstractNatsTest {

    @Test
    void simpleTest() throws JetStreamApiException, IOException {
        startContext();
        KeyValueStoreHolder keyValueStoreHolder = applicationContext.getBean(KeyValueStoreHolder.class);

        keyValueStoreHolder.put("test", "myvalue");

        Assertions.assertEquals("myvalue", keyValueStoreHolder.store.get("test").getValueAsString());
    }
}
