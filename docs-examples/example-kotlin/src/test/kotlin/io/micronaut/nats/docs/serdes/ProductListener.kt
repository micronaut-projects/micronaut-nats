package io.micronaut.nats.docs.serdes

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.docs.serdes.ProductInfo
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    val messages: MutableList<ProductInfo> = Collections.synchronizedList(ArrayList())

    @Subject("product")
    fun receive(productInfo: ProductInfo) {
        messages.add(productInfo)
    }
}
// end::clazz[]
