package io.micronaut.nats.docs.consumer.custom.type

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Subscription
import io.nats.client.impl.Headers
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    var messages: MutableList<ProductInfo> = Collections.synchronizedList(ArrayList())
    private var datas: MutableList<ByteArray> = Collections.synchronizedList(ArrayList())

    @Subject(value = "product")
    fun receive(data: ByteArray, productInfo: ProductInfo)  {// <1>
        messages.add(productInfo)
        datas.add(data)
    }

}
// end::clazz[]
