package io.micronaut.nats.jetstream

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.nats.annotation.NatsConnection
import io.micronaut.nats.jetstream.annotation.ObjectStore
import io.nats.client.JetStreamApiException
import io.nats.client.ObjectStoreManagement
import io.nats.client.api.ObjectInfo
import io.nats.client.api.ObjectMeta
import io.nats.client.api.ObjectStoreStatus
import jakarta.inject.Inject
import jakarta.inject.Singleton

class ObjectStoreSpec extends AbstractJetstreamTest {

    void "simple factory test"() {
        given:
        ApplicationContext applicationContext = startContext()
        ObjectStoreManagement osm = applicationContext.getBean(ObjectStoreManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))

        when:
        ObjectStoreStatus oss = osm.getStatus("examplestore")

        then:
        osm != null
        oss != null

        when:
        osm.delete("examplestore")
        osm.getStatus("examplestore")

        then:
        thrown JetStreamApiException

        cleanup:
        applicationContext.close()
    }

    void "full test"() {
        given:
        ApplicationContext applicationContext = startContext()
        ObjectStoreManagement osm = applicationContext.getBean(ObjectStoreManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))
        io.nats.client.ObjectStore os = applicationContext.getBean(ObjectStoreHolder).objectStore

        when:
        ObjectInfo objectInfo = os.put("my-file", "Byte Value 1".getBytes())
        ObjectInfo objectInfo1 = os.put("my-file-1", new ByteArrayInputStream("Byte Value 2".getBytes()))
        ObjectInfo objectInfo2 = os.put(ObjectMeta.builder("my-object").description("test").build(), new ByteArrayInputStream("Object Value 1".getBytes()))

        then:
        objectInfo != null
        objectInfo1 != null
        objectInfo2 != null

        when:
        os.delete("my-file-1")

        then:
        os.getInfo("my-file-1") == null
        os.getInfo("my-file-1", true) != null

        when:
        ObjectInfo oi = os.getInfo("not-found")

        then:
        oi == null

        cleanup:
        applicationContext.close()
    }


    @Requires(property = 'spec.name', value = 'ObjectStoreSpec')
    @Singleton
    static class ObjectStoreHolder {
        @Inject
        @ObjectStore('examplestore')
        io.nats.client.ObjectStore objectStore;
    }
}
