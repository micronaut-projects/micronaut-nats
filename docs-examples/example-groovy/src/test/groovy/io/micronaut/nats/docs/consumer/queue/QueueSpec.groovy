package io.micronaut.nats.docs.consumer.queue

import io.micronaut.nats.AbstractNatsTest

class QueueSpec extends AbstractNatsTest {

    void "test product client and listener"() {
        startContext()

        when:
// tag::producer[]
def productClient = applicationContext.getBean(ProductClient)
productClient.send("quickstart".bytes)
// end::producer[]

        io.micronaut.nats.docs.consumer.queue.ProductListener productListener = applicationContext.getBean(io.micronaut.nats.docs.consumer.queue.ProductListener)

        then:
        waitFor {
            productListener.messageLengths.size() == 1
            productListener.messageLengths[0] == "quickstart"
        }

        cleanup:
        // Finding that the context is closing the channel before ack is sent
        sleep 200
    }
}
