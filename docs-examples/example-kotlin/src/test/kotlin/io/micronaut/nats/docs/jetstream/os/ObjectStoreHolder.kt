package io.micronaut.nats.docs.jetstream.os

import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.nats.jetstream.annotation.ObjectStore
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.InputStream

// end::imports[]

@Requires(property = "spec.name", value = "ObjectStoreSpec")
// tag::clazz[]
@Singleton
class ObjectStoreHolder {

    @Inject
    @field:ObjectStore("examplebucket")
    lateinit var store: io.nats.client.ObjectStore // <1>

    fun put(key: String, value:InputStream) {
        store.put(key, value)
    }

}
// end::clazz[]
