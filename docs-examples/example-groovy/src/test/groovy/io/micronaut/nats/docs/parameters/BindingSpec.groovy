package io.micronaut.nats.docs.parameters

import io.micronaut.nats.AbstractNatsTest

class BindingSpec extends AbstractNatsTest {

    void "test dynamic binding"() {
        startContext()

        when:
// tag::producer[]
        def productClient = applicationContext.getBean(ProductClient)
        productClient.send("message body".bytes)
        productClient.send("product", "message body2".bytes)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        waitFor {
            productListener.messageLengths.size() == 2
            productListener.messageLengths.contains(12)
            productListener.messageLengths.contains(13)
        }
    }
}