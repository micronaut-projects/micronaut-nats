package io.micronaut.nats.docs.jetstream.kv

import io.micronaut.nats.AbstractNatsTest

class KeyValueSpec extends AbstractNatsTest {

    void "simple producer"() {
        startContext()

        when:
        def keyValueStoreHolder = applicationContext.getBean(KeyValueStoreHolder)
        keyValueStoreHolder.put("test", "myvalue")

        then:
        keyValueStoreHolder.store.get("test").valueAsString == 'myvalue'
    }
}
