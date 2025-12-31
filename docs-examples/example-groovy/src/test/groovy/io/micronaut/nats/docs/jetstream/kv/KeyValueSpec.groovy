package io.micronaut.nats.docs.jetstream.kv

import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "KeyValueSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeyValueSpec extends Specification implements TestPropertyProvider{
    @Inject KeyValueStoreHolder keyValueStoreHolder

    void "simple producer"() {
        when:
        keyValueStoreHolder.put("test", "myvalue")

        then:
        keyValueStoreHolder.store.get("test").valueAsString == 'myvalue'
    }

    @Override
    Map<String, String> getProperties() {
        Nats.properties
    }
}
