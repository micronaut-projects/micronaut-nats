package io.micronaut.nats.docs.jetstream.os;

import io.micronaut.nats.AbstractNatsTest;
import io.nats.client.JetStreamApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

class ObjectStoreTest extends AbstractNatsTest {

    @Test
    void simpleTest() throws JetStreamApiException, IOException, NoSuchAlgorithmException {
        startContext();
        ObjectStoreHolder objectStoreHolder = applicationContext.getBean(ObjectStoreHolder.class);

        objectStoreHolder.put("test", new ByteArrayInputStream("myvalue".getBytes()));

        Assertions.assertNotNull(objectStoreHolder.store.getInfo("test"));
    }
}
