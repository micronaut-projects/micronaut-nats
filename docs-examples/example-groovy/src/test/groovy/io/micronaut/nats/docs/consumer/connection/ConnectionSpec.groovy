package io.micronaut.nats.docs.consumer.connection

import io.micronaut.nats.AbstractNatsTest

class ConnectionSpec extends AbstractNatsTest {

    void "test product client and listener"() {
        startContext()

        when:
// tag::producer[]
        def productClient = applicationContext.getBean(ProductClient)
        productClient.send("connection-test".bytes)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        waitFor {
            productListener.messageLengths.size() == 1
            productListener.messageLengths[0] == "connection-test"
        }

        cleanup:
        // Finding that the context is closing the channel before ack is sent
        sleep 200
    }

    protected Map<String, Object> getConfiguration() {
        super.configuration + ["nats.product-cluster.addresses": super.configuration.remove("nats.default.addresses")]
    }
}
