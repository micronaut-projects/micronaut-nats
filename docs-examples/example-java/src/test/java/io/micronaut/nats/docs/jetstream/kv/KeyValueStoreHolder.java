package io.micronaut.nats.docs.jetstream.kv;


import io.micronaut.context.annotation.Requires;
// tag:imports[]
import io.micronaut.nats.jetstream.annotation.KeyValueStore;
import io.nats.client.JetStreamApiException;
import io.nats.client.KeyValue;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
// end::imports[]

@Requires(property = "spec.name", value = "KeyValueTest")
// tag::clazz[]
@Singleton
public class KeyValueStoreHolder {

    @Inject
    @KeyValueStore("examplebucket")
    KeyValue store; // <1>

    public void put(String key, String value) throws JetStreamApiException, IOException {
        store.put(key, value);
    }

}
// end::clazz[]
