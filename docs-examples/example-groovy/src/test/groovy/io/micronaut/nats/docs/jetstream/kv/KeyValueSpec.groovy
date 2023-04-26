package io.micronaut.nats.docs.jetstream.kv

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "KeyValueSpec")
class KeyValueSpec extends Specification {
    @Inject KeyValueStoreHolder keyValueStoreHolder

    void "simple producer"() {
        when:
        keyValueStoreHolder.put("test", "myvalue")

        then:
        keyValueStoreHolder.store.get("test").valueAsString == 'myvalue'
    }
}
