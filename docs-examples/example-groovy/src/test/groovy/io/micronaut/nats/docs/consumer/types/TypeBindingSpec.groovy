package io.micronaut.nats.docs.consumer.types

import io.micronaut.nats.AbstractNatsTest

class TypeBindingSpec extends AbstractNatsTest {

    void "test publishing and receiving rabbitmq types"() {
        startContext()

        when:
// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient)
        productClient.send("body".bytes, 20L)
        productClient.send("body2".bytes, 30L)
        productClient.send("body3".bytes, 40L)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        waitFor {
            productListener.messages.size() == 3
            productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]")
            productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]")
            productListener.messages.contains("subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]")
        }
    }
}
