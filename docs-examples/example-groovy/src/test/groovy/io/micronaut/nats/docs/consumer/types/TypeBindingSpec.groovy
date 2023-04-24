package io.micronaut.nats.docs.consumer.types

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

@MicronautTest
@Property(name = "spec.name", value = "TypeBindingSpec")
class TypeBindingSpec extends Specification {
    @Inject ProductClient productClient
    @Inject ProductListener productListener


    void "test publishing and receiving nats types"() {

        when:
// tag::producer[]
        productClient.send("body".bytes, 20L)
        productClient.send("body2".bytes, 30L)
        productClient.send("body3".bytes, 40L)
// end::producer[]


        then:
        await().atMost(10, SECONDS).until {
            productListener.messages.size() == 3
            productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]")
            productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]")
            productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]")
        }
    }
}
