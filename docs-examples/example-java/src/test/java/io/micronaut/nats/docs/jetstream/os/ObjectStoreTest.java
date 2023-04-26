package io.micronaut.nats.docs.jetstream.os;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.nats.client.JetStreamApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@MicronautTest
@Property(name = "spec.name", value = "ObjectStoreTest")
class ObjectStoreTest {

    @Test
    void simpleTest(ObjectStoreHolder objectStoreHolder) throws JetStreamApiException, IOException, NoSuchAlgorithmException {
        objectStoreHolder.put("test", new ByteArrayInputStream("myvalue".getBytes()));

        Assertions.assertNotNull(objectStoreHolder.store.getInfo("test"));
    }
}
