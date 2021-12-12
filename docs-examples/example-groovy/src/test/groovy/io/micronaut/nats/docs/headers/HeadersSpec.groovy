package io.micronaut.nats.docs.headers

import io.micronaut.nats.AbstractNatsTest
import io.nats.client.impl.Headers

class HeadersSpec extends AbstractNatsTest {

    void "test publishing and receiving headers"() {
        when:
        startContext()
// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient)
        productClient.send("body".bytes)
        productClient.send("medium", 20L, "body2".bytes)
        productClient.send(null, 30L, "body3".bytes)

        Headers headers = new Headers()
        headers.put("productSize", "large")
        headers.put("x-product-count", "40")
        productClient.send("body4".getBytes(), headers)
        productClient.send("body5".getBytes(), Arrays.asList("xtra-small", "xtra-large"))
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        waitFor {
            productListener.messageProperties.size() == 6
            productListener.messageProperties.contains("true|10|small")
            productListener.messageProperties.contains("true|20|medium")
            productListener.messageProperties.contains("true|30|medium")
            productListener.messageProperties.contains("true|40|large")
            productListener.messageProperties.contains("true|20|xtra-small")
            productListener.messageProperties.contains("true|20|xtra-large")
        }
    }
}
