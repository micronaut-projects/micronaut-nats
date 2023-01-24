package io.micronaut.nats.docs.jetstream.os

import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.ObjectStore
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

@Requires(property = "spec.name", value = "ObjectStoreSpec")
// tag::clazz[]
@Singleton
class ObjectStoreHolder {

    @Inject
    @ObjectStore("examplebucket")
    io.nats.client.ObjectStore store // <1>

    void put(String key, InputStream value) {
        store.put(key, value)
    }
}
// end::clazz[]
