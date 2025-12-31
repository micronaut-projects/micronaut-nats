package io.micronaut.nats.docs.jetstream.os

import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "ObjectStoreSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStoreSpec extends Specification implements TestPropertyProvider{
    @Inject ObjectStoreHolder objectStoreHolder

    void "simple producer"() {

        when:
        objectStoreHolder.put("test", new ByteArrayInputStream("myvalue".getBytes()))

        then:
        objectStoreHolder.store.getInfo("test") != null
    }

    @Override
    Map<String, String> getProperties() {
        Nats.properties
    }
}
