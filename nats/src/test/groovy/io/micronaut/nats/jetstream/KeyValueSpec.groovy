package io.micronaut.nats.jetstream

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.nats.annotation.NatsConnection
import io.micronaut.nats.jetstream.annotation.KeyValueStore
import io.nats.client.JetStreamApiException
import io.nats.client.KeyValue
import io.nats.client.KeyValueManagement
import io.nats.client.api.KeyValueConfiguration
import io.nats.client.api.KeyValueEntry
import io.nats.client.api.KeyValueStatus
import jakarta.inject.Inject
import jakarta.inject.Singleton

class KeyValueSpec extends AbstractJetstreamTest {

    private static final String BYTE_KEY = "byteKey";
    private static final String STRING_KEY = "stringKey";
    private static final String LONG_KEY = "longKey";
    private static final String NOT_FOUND = "notFound";

    void "simple factory test"() {
        given:
        ApplicationContext applicationContext = startContext()
        KeyValueManagement kvm = applicationContext.getBean(KeyValueManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))

        when:
        KeyValueStatus kvs = kvm.getStatus("examplebucket")

        then:
        kvm != null
        kvs != null

        when:
        KeyValueConfiguration kvc = KeyValueConfiguration.builder(kvs.getConfiguration())
                .description("Some new description")
                .maxHistoryPerKey(6)
                .build()
        kvs = kvm.update(kvc)

        then:
        kvs.description == "Some new description"
        kvs.maxHistoryPerKey == 6

        when:
        kvm.delete("examplebucket")
        kvm.getStatus("examplebucket")

        then:
        thrown JetStreamApiException

        cleanup:
        applicationContext.close()
    }

    void "full test"() {
        given:
        ApplicationContext applicationContext = startContext()
        KeyValueManagement kvm = applicationContext.getBean(KeyValueManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))
        KeyValue kv = applicationContext.getBean(KeyValueHolder).keyValueBucket

        when:
        kv.put(BYTE_KEY, "Byte Value 1".getBytes())
        kv.put(STRING_KEY, "String Value 1")
        def seq = kv.put(LONG_KEY, 1)

        then:
        seq == 3
        kv.get(BYTE_KEY).getValue() == "Byte Value 1".getBytes()
        kv.get(STRING_KEY).getValueAsString() == "String Value 1"
        kv.get(LONG_KEY).getValueAsLong() == 1

        when:
        kv.delete(BYTE_KEY)

        then:
        kv.get(BYTE_KEY) == null

        when:
        KeyValueEntry kve = kv.get(NOT_FOUND)

        then:
        kve == null

        when:
        kv.put(BYTE_KEY, "Byte Value 2".getBytes())
        kv.put(STRING_KEY, "String Value 2")
        seq = kv.put(LONG_KEY, 2)

        then:
        seq == 7
        kv.get(BYTE_KEY).getValue() == "Byte Value 2".getBytes()
        kv.get(STRING_KEY).getValueAsString() == "String Value 2"
        kv.get(LONG_KEY).getValueAsLong() == 2

        cleanup:
        applicationContext.close()
    }


    @Requires(property = 'spec.name', value = 'KeyValueSpec')
    @Singleton
    static class KeyValueHolder {
        @Inject
        @KeyValueStore('examplebucket')
        KeyValue keyValueBucket;
    }
}
