package io.micronaut.nats.docs.jetstream.os

import io.micronaut.nats.AbstractNatsTest

class ObjectStoreSpec extends AbstractNatsTest {

    void "simple producer"() {
        startContext()

        when:
        def objectStoreHolder = applicationContext.getBean(ObjectStoreHolder)
        objectStoreHolder.put("test", new ByteArrayInputStream("myvalue".getBytes()))

        then:
        objectStoreHolder.store.getInfo("test") != null
    }
}
