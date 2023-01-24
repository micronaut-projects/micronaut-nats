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
    @field:KeyValueStore("examplebucket")
    lateinit var store: KeyValue // <1>

    fun put(key: String, value:String) {
        store.put(key, value)
    }

}
// end::clazz[]
