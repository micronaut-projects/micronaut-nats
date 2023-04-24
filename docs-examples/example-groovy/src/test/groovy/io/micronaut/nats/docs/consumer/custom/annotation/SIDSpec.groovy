package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

@MicronautTest
@Property(name = "spec.name", value = "SIDSpec")
class SIDSpec extends Specification {
    @Inject ProductClient productClient
    @Inject ProductListener productListener


    void "test using a custom annotation binder"() {
        when:
// tag::producer[]
productClient.send("body".getBytes())
productClient.send("body2".getBytes())
productClient.send("body3".getBytes())
// end::producer[]

        then:
        await().atMost(10, SECONDS).until {
            productListener.messages.size() == 3
        }

        cleanup:
        sleep 200
    }
}
