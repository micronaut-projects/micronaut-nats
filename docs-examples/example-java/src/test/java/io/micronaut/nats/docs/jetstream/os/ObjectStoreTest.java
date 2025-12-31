package io.micronaut.nats.docs.jetstream.os;

import io.micronaut.context.annotation.Property;
import io.micronaut.nats.testcontainers.Nats;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.nats.client.JetStreamApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@MicronautTest
@Property(name = "spec.name", value = "ObjectStoreTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStoreTest implements TestPropertyProvider {

    @Test
    void simpleTest(ObjectStoreHolder objectStoreHolder) throws JetStreamApiException, IOException, NoSuchAlgorithmException {
        objectStoreHolder.put("test", new ByteArrayInputStream("myvalue".getBytes()));

        Assertions.assertNotNull(objectStoreHolder.store.getInfo("test"));
    }

    @Override
    public Map<String, String> getProperties() {
        return Nats.getProperties();
    }
}
