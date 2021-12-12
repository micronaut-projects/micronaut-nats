package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.nats.AbstractNatsTest

class SIDSpec extends AbstractNatsTest {

    void "test using a custom annotation binder"() {
        startContext()

        when:
// tag::producer[]
def productClient = applicationContext.getBean(ProductClient)
productClient.send("body".getBytes())
productClient.send("body2".getBytes())
productClient.send("body3".getBytes())
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        waitFor {
            productListener.messages.size() == 3
        }

        cleanup:
        sleep 200
    }
}
