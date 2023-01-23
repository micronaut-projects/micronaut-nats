package io.micronaut.nats.docs.jetstream.kv

import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.KeyValueStore
import io.nats.client.KeyValue
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

@Requires(property = "spec.name", value = "KeyValueSpec")
// tag::clazz[]
@Singleton
class KeyValueStoreHolder {

    @Inject
    @KeyValueStore("examplebucket")
    KeyValue store // <1>

    void put(String key, String value) {
        store.put(key, value)
    }
}
// end::clazz[]
