package io.micronaut.nats.docs.parameters

import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

@MicronautTest
@Property(name = "spec.name", value = "BindingSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BindingSpec extends Specification implements TestPropertyProvider{
    @Inject ProductClient productClient
    @Inject ProductListener productListener

    void "test dynamic binding"() {

        when:
// tag::producer[]
        productClient.send("message body".bytes)
        productClient.send("product", "message body2".bytes)
// end::producer[]

        then:
        await().atMost(10, SECONDS).until {
            productListener.messageLengths.size() == 2
            productListener.messageLengths.contains(12)
            productListener.messageLengths.contains(13)
        }
    }

    @Override
    Map<String, String> getProperties() {
        Nats.properties
    }
}
