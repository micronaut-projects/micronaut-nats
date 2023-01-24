package io.micronaut.nats.docs.jetstream.os;


import io.micronaut.context.annotation.Requires;

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.ObjectStore;
import io.nats.client.JetStreamApiException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
// end::imports[]

@Requires(property = "spec.name", value = "ObjectStoreTest")
// tag::clazz[]
@Singleton
public class ObjectStoreHolder {

    @Inject
    @ObjectStore("examplebucket")
    io.nats.client.ObjectStore store; // <1>

    public void put(String key, InputStream inputStream) throws JetStreamApiException, IOException, NoSuchAlgorithmException {
        store.put(key, inputStream);
    }

}
// end::clazz[]
