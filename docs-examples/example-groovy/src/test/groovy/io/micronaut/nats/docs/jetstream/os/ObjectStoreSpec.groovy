package io.micronaut.nats.docs.jetstream.os

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "ObjectStoreSpec")
class ObjectStoreSpec extends Specification {
    @Inject ObjectStoreHolder objectStoreHolder

    void "simple producer"() {

        when:
        objectStoreHolder.put("test", new ByteArrayInputStream("myvalue".getBytes()))

        then:
        objectStoreHolder.store.getInfo("test") != null
    }
}
